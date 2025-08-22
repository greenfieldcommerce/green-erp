package com.greenfieldcommerce.greenerp.records.contractorrate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import jakarta.validation.constraints.NotNull;

public record CreateContractorRateRecord(@NotNull BigDecimal rate, @NotNull Currency currency, @NotNull ZonedDateTime startDateTime, @NotNull ZonedDateTime endDateTime)
{
}
