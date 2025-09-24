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
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
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

	private static ZonedDateTime dateIsSameDay(final ZonedDateTime now)
	{
		return argThat(time -> now.toLocalDate().atStartOfDay().equals(time.toLocalDate().atStartOfDay()));
	}
}
