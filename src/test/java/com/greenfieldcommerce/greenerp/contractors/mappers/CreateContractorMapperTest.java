package com.greenfieldcommerce.greenerp.contractors.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateContractorMapperTest
{

	public static final String VALID_EMAIL = "valid@example.com";
	public static final String VALID_NAME = "Valid Name";

	private final CreateContractorMapper createContractorMapper = new CreateContractorMapper();

	@Test
	@DisplayName("Should map CreateContractorRecord to Contractor with valid data")
	void shouldMapCreateContractorRecordToContractorWithValidData()
	{
		CreateContractorRecord record = new CreateContractorRecord(VALID_EMAIL, VALID_NAME);

		Contractor result = createContractorMapper.map(record);

		assertNotNull(result);
		assertEquals(VALID_EMAIL, result.getEmail());
		assertEquals(VALID_NAME, result.getName());
	}
}
