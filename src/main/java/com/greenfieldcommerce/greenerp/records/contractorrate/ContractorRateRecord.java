package com.greenfieldcommerce.greenerp.records.contractorrate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ContractorRateRecord(
	Long id,
	BigDecimal rate,
	Currency currency,
	ZonedDateTime startDateTime,
	ZonedDateTime endDateTime)
{
}