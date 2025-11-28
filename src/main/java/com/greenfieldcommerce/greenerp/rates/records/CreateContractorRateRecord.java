package com.greenfieldcommerce.greenerp.rates.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateContractorRateRecord(
	@NotNull Long clientId,
	@DecimalMin(value = "100.00") @NotNull BigDecimal rate,
	@DecimalMin(value = "100.00") @NotNull BigDecimal externalRate,
	@DecimalMin(value = "1") @NotNull BigDecimal taxDeduction,
	@NotNull Currency currency,
	@NotNull ZonedDateTime startDateTime,
	@NotNull ZonedDateTime endDateTime)
{
}
