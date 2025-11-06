package com.greenfieldcommerce.greenerp.invoices.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Set;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "invoices")
public record ContractorInvoiceRecord(Long contractorId, ZonedDateTime startDate, ZonedDateTime endDate, BigDecimal numberOfWorkedDays, Set<InvoiceExtraAmountLineRecord> extraAmountLines, BigDecimal total, Currency currency) { }
