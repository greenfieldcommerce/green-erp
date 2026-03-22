package com.greenfieldcommerce.greenerp.rates.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.mappers.ClientToRecordMapper;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

@ExtendWith(MockitoExtension.class)
class ContractorRateToRecordMapperTest
{

	private static final Long CONTRACTOR_ID = 1L;
	private static final Long ID = 2L;
	private static final Long CLIENT_ID = 3L;
	private static final String CLIENT_NAME = "Test Client";
	private static final String CLIENT_EMAIL = "test@client.com";
	private static final BigDecimal RATE = BigDecimal.valueOf(300);
	private static final Currency CURRENCY = Currency.getInstance("USD");
	private static final ZonedDateTime START = ZonedDateTime.now();
	private static final ZonedDateTime END = START.plusMonths(12);

	@Mock
	private ClientToRecordMapper clientToRecordMapper;

	private ContractorRateToRecordMapper mapper;

	@BeforeEach
	void setUp()
	{
		mapper = new ContractorRateToRecordMapper(clientToRecordMapper);
	}

	@Test
	@DisplayName("Should map ContractorRate to ContractorRateRecord with valid data")
	void shouldMapContractorRateToContractorRateRecordWithValidData()
	{
		ContractorRate contractorRate = validContractorRate();
		ClientRecord expectedClientRecord = new ClientRecord(CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL);
		when(clientToRecordMapper.map(contractorRate.getClient())).thenReturn(expectedClientRecord);

		ContractorRateRecord result = mapper.map(contractorRate);

		assertNotNull(result);

		assertEquals(ID, result.id());
		assertEquals(RATE, result.rate());
		assertEquals(CURRENCY, result.currency());
		assertEquals(START, result.startDateTime());
		assertEquals(END, result.endDateTime());
		assertEquals(CONTRACTOR_ID, result.contractorId());
		assertEquals(expectedClientRecord, result.client());
		assertEquals(CLIENT_ID, result.client().id());
		assertEquals(CLIENT_NAME, result.client().name());
		assertEquals(CLIENT_EMAIL, result.client().email());
	}

	private ContractorRate validContractorRate()
	{
		ContractorRate rate = mock(ContractorRate.class);
		Contractor contractor = mock(Contractor.class);
		Client client = mock(Client.class);
		when(contractor.getId()).thenReturn(CONTRACTOR_ID);

		when(rate.getId()).thenReturn(ID);
		when(rate.getContractor()).thenReturn(contractor);
		when(rate.getClient()).thenReturn(client);
		when(rate.getRate()).thenReturn(RATE);
		when(rate.getCurrency()).thenReturn(CURRENCY);
		when(rate.getStartDateTime()).thenReturn(START);
		when(rate.getEndDateTime()).thenReturn(END);

		return rate;
	}
}
