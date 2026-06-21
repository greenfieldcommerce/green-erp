package com.greenfieldcommerce.greenerp.contractors.invoices.records;

import java.math.BigDecimal;

public record InvoiceExtraAmountLineRecord(Long id, BigDecimal amount, String description) {}
