package com.greenfieldcommerce.greenerp.contractors.invoices.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Set;

public record ContractorInvoiceRecord(Long contractorId, Long invoiceId, Long clientId, ZonedDateTime startDate, ZonedDateTime endDate, BigDecimal numberOfWorkedDays, Set<InvoiceExtraAmountLineRecord> extraAmountLines, BigDecimal total, Currency currency, String status) { }
