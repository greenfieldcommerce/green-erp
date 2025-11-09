package com.greenfieldcommerce.greenerp.invoices.records;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInvoiceExtraAmountLineRecord(@DecimalMin(value = "0.01") @NotNull BigDecimal amount, @NotBlank String description) {}
