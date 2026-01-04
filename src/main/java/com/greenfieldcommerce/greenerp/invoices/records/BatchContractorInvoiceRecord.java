package com.greenfieldcommerce.greenerp.invoices.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record BatchContractorInvoiceRecord(@NotNull Long contractorId, @NotNull Long clientId, @NotNull ZonedDateTime startDate, @NotNull ZonedDateTime endDate, @DecimalMin(value = "0") @DecimalMax(value = "31") BigDecimal numberOfWorkedDays) { }
