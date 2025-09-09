package com.greenfieldcommerce.greenerp.mappers.contractorrate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Currency;

@ExtendWith(MockitoExtension.class)
class ContractorRateToRecordMapperTest
{

	private static final Long ID = 1L;
	private static final BigDecimal RATE = BigDecimal.valueOf(300);
	private static final Currency CURRENCY = Currency.getInstance("USD");
	private static final ZonedDateTime START = ZonedDateTime.now();
	private static final ZonedDateTime END = START.plusMonths(12);

	private final ContractorRateToRecordMapper mapper = new ContractorRateToRecordMapper();

	@Test
	@DisplayName("Should map ContractorRate to ContractorRateRecord with valid data")
	void shouldMapContractorRateToContractorRateRecordWithValidData()
	{
		ContractorRate contractorRate = validContractorRate();
		ContractorRateRecord result = mapper.map(contractorRate);

		assertNotNull(result);
		assertEquals(ID, result.id());
		assertEquals(RATE, result.rate());
		assertEquals(CURRENCY, result.currency());
		assertEquals(START, result.startDateTime());
		assertEquals(END, result.endDateTime());
	}

	private ContractorRate validContractorRate()
	{
		ContractorRate rate = mock(ContractorRate.class);

		when(rate.getId()).thenReturn(ID);
		when(rate.getRate()).thenReturn(RATE);
		when(rate.getCurrency()).thenReturn(CURRENCY);
		when(rate.getStartDateTime()).thenReturn(START);
		when(rate.getEndDateTime()).thenReturn(END);

		return rate;
	}
}