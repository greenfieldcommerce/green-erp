package com.greenfieldcommerce.greenerp.invoices.records;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvoiceExtraAmountLineRecord(@DecimalMin(value = "0") @NotNull BigDecimal extraAmount, @NotBlank String description) {}
