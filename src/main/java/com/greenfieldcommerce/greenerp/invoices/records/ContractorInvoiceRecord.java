package com.greenfieldcommerce.greenerp.invoices.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "invoices")
public record ContractorInvoiceRecord(Long contractorId, ZonedDateTime startDate, ZonedDateTime endDate, BigDecimal numberOfWorkedDays, BigDecimal extraAmount, BigDecimal total, Currency currency) { }
