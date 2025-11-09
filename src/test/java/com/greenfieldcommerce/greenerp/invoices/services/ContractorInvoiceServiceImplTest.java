package com.greenfieldcommerce.greenerp.invoices.services;

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

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.rates.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;

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

		when(currentRateForContractor.getRate()).thenReturn(new BigDecimal(100));
		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.empty());
		when(contractorRateService.findCurrentRateForContractor(contractor)).thenReturn(currentRateForContractor);
		when(contractorInvoiceRepository.save(
			argThat(i -> i.getRate().equals(currentRateForContractor) && i.getNumberOfWorkedDays().equals(workedDays)))).thenReturn(saved);
		when(contractorInvoiceToRecordMapper.map(eq(saved))).thenReturn(savedRecord);

		final ContractorInvoiceRecord result = service.create(VALID_RESOURCE_ID, workedDays);

		assertEquals(savedRecord, result);
		verify(contractorInvoiceMessagingService).sendContractorInvoiceCreatedMessage(eq(savedRecord));
	}

	@Test
	@DisplayName("Should throw DuplicateContractorInvoiceException when trying to create invoice if one already exists in the current period")
	public void shouldThrowDuplicateContractorInvoiceExceptionWhenCreatingInvoiceIfOneAlreadyExistsInTheCurrentPeriod()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice existing = mock(ContractorInvoice.class);

		final BigDecimal workedDays = new BigDecimal(22);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now))).thenReturn(Optional.of(existing));

		assertThrows(DuplicateContractorInvoiceException.class, () -> service.create(VALID_RESOURCE_ID, workedDays));
		verify(contractorInvoiceRepository).findCurrentContractorInvoice(eq(contractor), dateIsSameDay(now));
	}

	@Test
	@DisplayName("Should find an invoice by contractor Id and invoice Id")
	public void shouldFindAnInvoiceByContractorIdAndInvoiceId()
	{
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice invoice = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord expectedRecord = mock(ContractorInvoiceRecord.class);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findByContractorAndId(eq(contractor), eq(VALID_RESOURCE_ID))).thenReturn(Optional.of(invoice));
		when(contractorInvoiceToRecordMapper.map(eq(invoice))).thenReturn(expectedRecord);

		final ContractorInvoiceRecord contractorInvoiceRecord = service.findByContractorAndId(VALID_RESOURCE_ID, VALID_RESOURCE_ID);
		assertEquals(expectedRecord, contractorInvoiceRecord);
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when searching an inexistent invoice")
	public void shouldThrowEntityNotFoundExceptionWhenSearchingAnInexistentInvoice()
	{
		final Contractor contractor = mock(Contractor.class);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findByContractorAndId(eq(contractor), eq(0L))).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.findByContractorAndId(VALID_RESOURCE_ID, 0L));
	}

	@Test
	@DisplayName("Should add an extra amount line to an existing invoice")
	public void shouldAddAnExtraAmountLineToAnExistingInvoice()
	{
		final ContractorInvoice invoice = mock(ContractorInvoice.class);
		final ContractorInvoice	saved = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord expectedRecord = mock(ContractorInvoiceRecord.class);

		final InvoiceExtraAmountLineRecord extraAmountLineRecord = new InvoiceExtraAmountLineRecord(BigDecimal.valueOf(100), "Extra Amount");

		when(contractorInvoiceRepository.findById(VALID_RESOURCE_ID)).thenReturn(Optional.of(invoice));
		when(contractorInvoiceRepository.save(invoice)).thenReturn(saved);
		when(contractorInvoiceToRecordMapper.map(eq(saved))).thenReturn(expectedRecord);

		final ContractorInvoiceRecord contractorInvoiceRecord = service.addExtraAmountLineToInvoice(VALID_RESOURCE_ID, extraAmountLineRecord);

		verify(invoice).addExtraAmountLine(argThat(l -> l.getAmount().equals(extraAmountLineRecord.extraAmount()) && l.getDescription().equals(extraAmountLineRecord.description())));
		assertEquals(expectedRecord, contractorInvoiceRecord);
	}

	@Test
	@DisplayName("Should update invoice for contractor")
	public void shouldUpdateInvoiceForContractor()
	{
		final Contractor contractor = mock(Contractor.class);
		final ContractorInvoice invoice = mock(ContractorInvoice.class);
		final ContractorInvoice saved = mock(ContractorInvoice.class);
		final ContractorInvoiceRecord invoiceRecord = mock(ContractorInvoiceRecord.class);

		final BigDecimal workedDays = new BigDecimal(22);

		when(contractorService.findEntityById(VALID_RESOURCE_ID)).thenReturn(contractor);
		when(contractorInvoiceRepository.findByContractorAndId(eq(contractor), eq(VALID_RESOURCE_ID))).thenReturn(Optional.of(invoice));
		when(contractorInvoiceRepository.save(invoice)).thenReturn(saved);
		when(contractorInvoiceToRecordMapper.map(eq(saved))).thenReturn(invoiceRecord);

		final ContractorInvoiceRecord result = service.patchInvoice(VALID_RESOURCE_ID, VALID_RESOURCE_ID, workedDays);
		assertEquals(invoiceRecord, result);
		verify(invoice).setNumberOfWorkedDays(workedDays);
		verify(contractorInvoiceRepository).save(invoice);
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

	private static ZonedDateTime dateIsSameDay(final ZonedDateTime now)
	{
		return argThat(time -> now.toLocalDate().atStartOfDay().equals(time.toLocalDate().atStartOfDay()));
	}
}
