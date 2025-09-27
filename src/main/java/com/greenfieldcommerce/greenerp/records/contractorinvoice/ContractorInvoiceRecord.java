package com.greenfieldcommerce.greenerp.records.contractorinvoice;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

public record ContractorInvoiceRecord(Long contractorId, ZonedDateTime startDate, ZonedDateTime endDate, BigDecimal numberOfWorkedDays, BigDecimal extraAmount, BigDecimal total, Currency currency) { }
