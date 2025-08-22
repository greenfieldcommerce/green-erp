package com.greenfieldcommerce.greenerp.records.contractorinvoice;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

public record ContractorInvoiceRecord(ZonedDateTime startDate, ZonedDateTime endDate, BigDecimal numberOfWorkedDays, BigDecimal total, BigDecimal extraAmount, Currency currency) { }
