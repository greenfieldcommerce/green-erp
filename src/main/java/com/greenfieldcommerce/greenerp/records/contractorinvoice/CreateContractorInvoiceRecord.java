package com.greenfieldcommerce.greenerp.records.contractorinvoice;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateContractorInvoiceRecord(@DecimalMin(value = "0") @DecimalMax(value = "31") @NotNull BigDecimal numberOfWorkedDays, @DecimalMin(value = "0") @NotNull BigDecimal extraAmount) { }
