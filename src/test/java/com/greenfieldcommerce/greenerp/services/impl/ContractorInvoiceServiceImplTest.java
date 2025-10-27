package com.greenfieldcommerce.greenerp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceMessagingService;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.services.ContractorService;

@ExtendWith(MockitoExtension.class)
public class ContractorInvoiceServiceImplTest
{
	private static final Long VALID_RESOURCE_ID = 1L;

	@Mock
	private ContractorInvoiceRepository contractorInvoiceRepository;
	@Mock
	private ContractorRateService contractorRateService;
	@Mock
	private Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;
	@Mock
	private ContractorService contractorService;
	@Mock
	private ContractorInvoiceMessagingService contractorInvoiceMessagingService;

	@InjectMocks
	private ContractorInvoiceServiceImpl service;

	@Test
	@DisplayName("Should throw DuplicateContractorInvoiceException when trying to create invoice if one already exists in the current period")
	public void shouldThrowDuplicateContractorInvoiceExceptionWhenCreatingInvoiceIfOneAlreadyExistsInTheCurrentPeriod()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice existing = mock(ContractorInvoice.class);

		final BigDecimal workedDays = new BigDecimal(22);
		final BigDecimal extra = new BigDecimal(100);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.of(existing));

		assertThrows(DuplicateContractorInvoiceException.class, () -> service.create(VALID_RESOURCE_ID, workedDays, extra));
		verify(contractorInvoiceRepository).findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now));
	}

	@Test
	@DisplayName("Should create contractor invoice")
	public void shouldCreateContractorInvoice()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorRate currentRateForContractor = mock(ContractorRate.class);
		final ContractorInvoice saved = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord savedRecord = mock(ContractorInvoiceRecord.class);

		final BigDecimal workedDays = new BigDecimal(22);
		final BigDecimal extra = new BigDecimal(100);

		when(currentRateForContractor.getRate()).thenReturn(new BigDecimal(100));
		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.empty());
		when(contractorRateService.findCurrentRateForContractor(contractor)).thenReturn(currentRateForContractor);
		when(contractorInvoiceRepository.save(
			argThat(i -> i.getRate().equals(currentRateForContractor)
				&& i.getNumberOfWorkedDays().equals(workedDays) && i.getExtraAmount().equals(extra)))).thenReturn(saved);
		when(contractorInvoiceToRecordMapper.map(eq(saved))).thenReturn(savedRecord);

		final ContractorInvoiceRecord result = service.create(VALID_RESOURCE_ID, workedDays, extra);

		assertEquals(savedRecord, result);
		verify(contractorInvoiceMessagingService).sendContractorInvoiceCreatedMessage(eq(savedRecord));
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when trying to find current invoice for contractor if invoice does not exist")
	public void shouldThrowEntityNotFoundExceptionWhenTryingToFindCurrentInvoiceForContractorIfInvoiceDoesNotExist()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.findCurrentInvoiceForContractor(VALID_RESOURCE_ID));
	}

	@Test
	@DisplayName("Should return current invoice for contractor")
	public void shouldReturnCurrentInvoiceForContractor()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice invoice = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord invoiceRecord = mock(ContractorInvoiceRecord.class);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.of(invoice));
		when(contractorInvoiceToRecordMapper.map(eq(invoice))).thenReturn(invoiceRecord);

		final ContractorInvoiceRecord result = service.findCurrentInvoiceForContractor(VALID_RESOURCE_ID);
		assertEquals(invoiceRecord, result);
	}

	@Test
	@DisplayName("Should update current invoice for contractor")
	public void shouldUpdateCurrentInvoiceForContractor()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice invoice = mock(ContractorInvoice.class);
		final ContractorInvoice saved = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord invoiceRecord = mock(ContractorInvoiceRecord.class);

		final BigDecimal workedDays = new BigDecimal(22);
		final BigDecimal extra = new BigDecimal(100);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.of(invoice));
		when(contractorInvoiceRepository.save(invoice)).thenReturn(saved);
		when(contractorInvoiceToRecordMapper.map(eq(saved))).thenReturn(invoiceRecord);

		final ContractorInvoiceRecord result = service.patchInvoice(VALID_RESOURCE_ID, workedDays, extra);
		assertEquals(invoiceRecord, result);
		verify(invoice).setNumberOfWorkedDays(workedDays);
		verify(invoice).setExtraAmount(extra);
		verify(contractorInvoiceRepository).save(invoice);
	}

	@Test
	@DisplayName("Should find the sorted latest x invoices for contractor")
	public void shouldFindTheSortedLatestXInvoicesForContractor()
	{
		final Contractor contractor = mock(Contractor.class);
		final Sort sort = Sort.by(Sort.Direction.DESC, "invoiceDate");
		final Pageable pageable = PageRequest.of(0, 2, sort);
		final ContractorInvoice invoice1 = mock(ContractorInvoice.class);
		final ContractorInvoice invoice2 = mock(ContractorInvoice.class);

		final ContractorInvoiceRecord invoice1Record = mock(ContractorInvoiceRecord.class);
		final ContractorInvoiceRecord invoice2Record = mock(ContractorInvoiceRecord.class);

		final List<ContractorInvoice> invoices = List.of(invoice1, invoice2);
		final Page<ContractorInvoice> page = new PageImpl<>(invoices, pageable, invoices.size());

		when(contractorInvoiceRepository.findByContractor(eq(contractor), eq(pageable))).thenReturn(page);
		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceToRecordMapper.map(eq(invoice1))).thenReturn(invoice1Record);
		when(contractorInvoiceToRecordMapper.map(eq(invoice2))).thenReturn(invoice2Record);

		final Page<ContractorInvoiceRecord> result = service.findByContractor(VALID_RESOURCE_ID, pageable);
		assertEquals(2, result.getNumberOfElements());
		assertEquals(invoice1Record, result.getContent().get(0));
		assertEquals(invoice2Record, result.getContent().get(1));
		assertEquals(invoices.size(), result.getTotalElements());
		assertEquals(pageable, result.getPageable());
	}

	private static ZonedDateTime dateIsSameDay(final ZonedDateTime now)
	{
		return argThat(time -> now.toLocalDate().atStartOfDay().equals(time.toLocalDate().atStartOfDay()));
	}
}
