package com.greenfieldcommerce.greenerp.records.contractorinvoice;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record CreateContractorInvoiceRecord(@NotNull BigDecimal numberOfWorkedDays, @NotNull BigDecimal extraAmount) { }
