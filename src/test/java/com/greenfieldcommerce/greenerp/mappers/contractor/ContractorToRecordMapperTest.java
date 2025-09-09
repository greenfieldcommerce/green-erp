package com.greenfieldcommerce.greenerp.mappers.contractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractorToRecordMapperTest
{

	public static final long VALID_CONTRACTOR_ID = 1L;
	public static final String VALID_CONTRACTOR_EMAIL = "test@example.com";
	public static final String VALID_CONTRACTOR_NAME = "Test Contractor";
	@Mock
	private Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	@InjectMocks
	private ContractorToRecordMapper contractorToRecordMapper;

	@Test
	@DisplayName("Should map Contractor to ContractorRecord without current rate")
	void shouldMapContractorToContractorRecordWithoutCurrentRate()
	{
		Contractor contractor = validContractor();
		when(contractor.getCurrentRate()).thenReturn(Optional.empty());

		ContractorRecord result = contractorToRecordMapper.map(contractor);

		assertNotNull(result);
		assertEquals(VALID_CONTRACTOR_ID, result.id());
		assertEquals(VALID_CONTRACTOR_EMAIL, result.email());
		assertEquals(VALID_CONTRACTOR_NAME, result.name());
		assertNull(result.currentRate());
	}


	@Test
	@DisplayName("Should map Contractor to ContractorRecord with current rate")
	void shouldMapContractorToContractorRecordWithCurrentRate()
	{
		Contractor contractor = validContractor();

		ContractorRate contractorRate = mock(ContractorRate.class);
		when(contractor.getCurrentRate()).thenReturn(Optional.of(contractorRate));

		ContractorRateRecord contractorRateRecord = mock(ContractorRateRecord.class);
		when(contractorRateToRecordMapper.map(contractorRate)).thenReturn(contractorRateRecord);

		ContractorRecord result = contractorToRecordMapper.map(contractor);

		assertNotNull(result);
		assertEquals(VALID_CONTRACTOR_ID, result.id());
		assertEquals(VALID_CONTRACTOR_EMAIL, result.email());
		assertEquals(VALID_CONTRACTOR_NAME, result.name());
		assertEquals(contractorRateRecord, result.currentRate());
	}

	Contractor validContractor()
	{
		Contractor contractor = mock(Contractor.class);
		when(contractor.getId()).thenReturn(VALID_CONTRACTOR_ID);
		when(contractor.getEmail()).thenReturn(VALID_CONTRACTOR_EMAIL);
		when(contractor.getName()).thenReturn(VALID_CONTRACTOR_NAME);

		return contractor;
	}
}