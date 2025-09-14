package com.greenfieldcommerce.greenerp.services.impl;

import static config.ResolverTestConfig.INVALID_RESOURCE_ID;
import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.exceptions.OverlappingContractorRateException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;
import com.greenfieldcommerce.greenerp.services.ContractorService;

@ExtendWith(MockitoExtension.class)
public class ContractorRateServiceImplTest
{

	@Mock
	private ContractorService contractorService;
	@Mock
	private ContractorRateRepository contractorRateRepository;
	@Mock
	private Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	@InjectMocks
	private ContractorRateServiceImpl service;

	@Test
	@DisplayName("Should find rates for contractor, ordered by end date time - desc")
	public void shouldFindRatesForContractor()
	{
		ContractorRate first = mock(ContractorRate.class);
		ContractorRate second = mock(ContractorRate.class);

		ContractorRateRecord firstRecord = mock(ContractorRateRecord.class);
		ContractorRateRecord secondRecord = mock(ContractorRateRecord.class);

		when(contractorRateRepository.findByContractorIdOrderByEndDateTimeDesc(VALID_RESOURCE_ID)).thenReturn(List.of(first, second));
		when(contractorRateToRecordMapper.map(eq(first))).thenReturn(firstRecord);
		when(contractorRateToRecordMapper.map(eq(second))).thenReturn(secondRecord);

		List<ContractorRateRecord> result = service.findRatesForContractor(VALID_RESOURCE_ID);

		assertEquals(2, result.size());
		assertEquals(firstRecord, result.getFirst());
		assertEquals(secondRecord, result.getLast());
		verify(contractorRateRepository).findByContractorIdOrderByEndDateTimeDesc(VALID_RESOURCE_ID);
	}

	@Test
	@DisplayName("Should return an empty list when contractor has no rate")
	public void shouldReturnEmptyListWhenContractorHasNoRate()
	{
		when(contractorRateRepository.findByContractorIdOrderByEndDateTimeDesc(VALID_RESOURCE_ID)).thenReturn(new ArrayList<>());
		List<ContractorRateRecord> result = service.findRatesForContractor(VALID_RESOURCE_ID);

		assertEquals(0, result.size());
		verify(contractorRateRepository).findByContractorIdOrderByEndDateTimeDesc(VALID_RESOURCE_ID);
	}

	@Test
	@DisplayName("Should return contractor rate by id and contractor id")
	public void shouldReturnContractorRateById()
	{
		final ContractorRate rate = mock(ContractorRate.class);
		final ContractorRateRecord rateRecord = mock(ContractorRateRecord.class);

		when(contractorRateRepository.findByIdAndContractorId(eq(1L), eq(2L))).thenReturn(Optional.of(rate));
		when(contractorRateToRecordMapper.map(eq(rate))).thenReturn(rateRecord);

		final ContractorRateRecord result = service.findByIdAndContractorId(1L, 2L);

		assertEquals(rateRecord, result);
		verify(contractorRateRepository).findByIdAndContractorId(eq(1L), eq(2L));
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when cannot find a contractor rate by id and contractor id")
	public void shouldThrowEntityNotFoundExceptionWhenCannotFindAContractorRateByIdAndContractorId()
	{
		when(contractorRateRepository.findByIdAndContractorId(eq(1L), eq(2L))).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.findByIdAndContractorId(1L, 2L));
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when trying to create a rate for inexistent contractor")
	public void shouldThrowEntityNotFoundExceptionWhenCreatingRateForInexistentContractor()
	{
		CreateContractorRateRecord record = mock(CreateContractorRateRecord.class);
		when(contractorService.findEntityById(eq(INVALID_RESOURCE_ID))).thenThrow(new EntityNotFoundException("ERROR", "Contractor not found"));

		assertThrows(EntityNotFoundException.class, () -> service.create(INVALID_RESOURCE_ID, record));
		verify(contractorRateRepository, never()).save(any(ContractorRate.class));
	}

	@Test
	@DisplayName("Should throw OverlappingContractorRateException when trying to create an overlapping rate")
	public void shouldThrowOverlappingContractorRateExceptionWhenCreatingOverlappingRate()
	{
		final Contractor contractor = mock(Contractor.class);
		final ContractorRate overlapping = mock(ContractorRate.class);
		final CreateContractorRateRecord record = mock(CreateContractorRateRecord.class);

		when(record.startDateTime()).thenReturn(ZonedDateTime.now());
		when(record.endDateTime()).thenReturn(ZonedDateTime.now().plusMonths(1));
		when(contractorService.findEntityById(eq(VALID_RESOURCE_ID))).thenReturn(contractor);
		mockOverlappingLookup(contractor, record.startDateTime(), record.endDateTime(),null, List.of(overlapping));

		assertThrows(OverlappingContractorRateException.class, () -> service.create(VALID_RESOURCE_ID, record));
		verify(contractorRateRepository, never()).save(any(ContractorRate.class));
	}

	@Test
	@DisplayName("Should create contractor rate")
	public void shouldCreateContractorRate()
	{
		final Contractor contractor = mock(Contractor.class);
		final CreateContractorRateRecord record = mockRecord();
		final ContractorRate saved = mock(ContractorRate.class);
		final ContractorRateRecord savedRecord = mock(ContractorRateRecord.class);

		when(contractorService.findEntityById(eq(VALID_RESOURCE_ID))).thenReturn(contractor);
		mockOverlappingLookup(contractor, record.startDateTime(), record.endDateTime(),null, new ArrayList<>());
		when(contractorRateRepository.save(argThat(matchesContractor(contractor, record)))).thenReturn(saved);
		when(contractorRateToRecordMapper.map(eq(saved))).thenReturn(savedRecord);

		final ContractorRateRecord result = service.create(VALID_RESOURCE_ID, record);
		verify(contractorRateRepository).save(any(ContractorRate.class));
		verify(contractorRateRepository).findRatesForContractorIdOverlappingWithPeriod(
			eq(contractor),
			argThat(d -> d.toInstant().equals(record.startDateTime().toInstant())),
			argThat(d -> d.toInstant().equals(record.endDateTime().toInstant())),
			eq(null));

		assertEquals(savedRecord, result);
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when trying to update end date time with inexistent rate")
	public void shouldThrowEntityNotFoundExceptionWhenUpdatingEndDateTimeWithInexistentRate()
	{
		when(contractorRateRepository.findByIdAndContractorId(eq(INVALID_RESOURCE_ID), eq(INVALID_RESOURCE_ID))).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.changeEndDateTime(INVALID_RESOURCE_ID, INVALID_RESOURCE_ID, ZonedDateTime.now()));
		verify(contractorRateRepository, never()).save(any(ContractorRate.class));
	}

	@Test
	@DisplayName("Should throw OverlappingContractorRateException when trying to update end date time with overlapping period")
	public void shouldThrowOverlappingContractorRateExceptionWhenUpdatingEndDateTimeWithOverlappingPeriod()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorRate existing = mock(ContractorRate.class);
		final ContractorRate overlapping = mock(ContractorRate.class);

		when(existing.getContractor()).thenReturn(contractor);
		when(existing.getStartDateTime()).thenReturn(ZonedDateTime.now());
		when(existing.getId()).thenReturn(VALID_RESOURCE_ID);

		when(contractorRateRepository.findByIdAndContractorId(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID))).thenReturn(Optional.of(existing));
		mockOverlappingLookup(contractor, existing.getStartDateTime(), now, VALID_RESOURCE_ID, List.of(overlapping));

		assertThrows(OverlappingContractorRateException.class, () -> service.changeEndDateTime(VALID_RESOURCE_ID, VALID_RESOURCE_ID, now));
		verify(contractorRateRepository, never()).save(any(ContractorRate.class));
	}

	@Test
	@DisplayName("Should update end date time")
	public void shouldUpdateEndDateTime()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		final Contractor contractor = mock(Contractor.class);
		final ContractorRate existing = mock(ContractorRate.class);
		final ContractorRate saved = mock(ContractorRate.class);
		final ContractorRateRecord savedRecord = mock(ContractorRateRecord.class);

		when(existing.getContractor()).thenReturn(contractor);
		when(existing.getStartDateTime()).thenReturn(ZonedDateTime.now());
		when(existing.getId()).thenReturn(VALID_RESOURCE_ID);

		when(contractorRateRepository.findByIdAndContractorId(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID))).thenReturn(Optional.of(existing));
		mockOverlappingLookup(contractor, existing.getStartDateTime(), now, VALID_RESOURCE_ID, new ArrayList<>());
		when(contractorRateRepository.save(existing)).thenReturn(saved);
		when(contractorRateToRecordMapper.map(eq(saved))).thenReturn(savedRecord);

		final ContractorRateRecord contractorRateRecord = service.changeEndDateTime(VALID_RESOURCE_ID, VALID_RESOURCE_ID, now);

		assertEquals(savedRecord, contractorRateRecord);
		verify(existing).setEndDateTime(argThat(d -> d.toInstant().equals(now.toInstant())));
	}

	@Test
	@DisplayName("Should throw NoActiveContractorRateException when there is no active rate for contractor")
	public void shouldThrowNoActiveContractorRateExceptionWhenThereIsNoActiveRateForContractor()
	{
		final Contractor contractor = mock(Contractor.class);
		when(contractor.getCurrentRate()).thenReturn(Optional.empty());

		assertThrows(NoActiveContractorRateException.class, () -> service.findCurrentRateForContractor(contractor));
	}

	@Test
	@DisplayName("Should find current rate for contractor")
	public void shouldFindCurrentRateForContractor()
	{
		final Contractor contractor = mock(Contractor.class);
		final ContractorRate rate = mock(ContractorRate.class);
		when(contractor.getCurrentRate()).thenReturn(Optional.of(rate));

		final ContractorRate result = service.findCurrentRateForContractor(contractor);
		assertEquals(rate, result);
	}

	@Test
	@DisplayName("Should delete contractor rate")
	public void shouldDeleteContractorRate()
	{
		service.delete(VALID_RESOURCE_ID, VALID_RESOURCE_ID);
		verify(contractorRateRepository).deleteByContractorIdAndId(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID));
	}

	private static ArgumentMatcher<ContractorRate> matchesContractor(final Contractor contractor, final CreateContractorRateRecord record)
	{
		return rate -> {
			boolean matchesId = rate.getId() == null;
			boolean matchesContractor = rate.getContractor().equals(contractor);
			boolean matchesRate = rate.getRate().equals(record.rate());
			boolean matchesCurrency = rate.getCurrency().equals(record.currency());
			boolean matchesStartDateTime = rate.getStartDateTime().toInstant().equals(record.startDateTime().toInstant());
			boolean matchesEndDateTime = rate.getEndDateTime().toInstant().equals(record.endDateTime().toInstant());
			return matchesId && matchesContractor && matchesRate && matchesCurrency && matchesStartDateTime && matchesEndDateTime;
		};
	}

	private CreateContractorRateRecord mockRecord()
	{
		final CreateContractorRateRecord record = mock(CreateContractorRateRecord.class);
		when(record.rate()).thenReturn(BigDecimal.valueOf(100));
		when(record.currency()).thenReturn(Currency.getInstance("USD"));
		when(record.startDateTime()).thenReturn(ZonedDateTime.now());
		when(record.endDateTime()).thenReturn(ZonedDateTime.now().plusMonths(1));
		return record;
	}

	private void mockOverlappingLookup(final Contractor contractor, final ZonedDateTime start, final ZonedDateTime end, final Long excludedId, List<ContractorRate> expectedReturn)
	{
		when(contractorRateRepository.findRatesForContractorIdOverlappingWithPeriod(eq(contractor), argThat(d -> d.toInstant().equals(start.toInstant())), argThat(d -> d.toInstant().equals(end.toInstant())), eq(excludedId))).thenReturn(expectedReturn);
	}

}
