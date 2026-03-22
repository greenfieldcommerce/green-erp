package com.greenfieldcommerce.greenerp.rates.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import org.springframework.hateoas.server.core.Relation;

import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

@Relation(collectionRelation = "rates")
public record ContractorRateRecord(Long id, Long contractorId, ClientRecord client, BigDecimal rate, Currency currency, ZonedDateTime startDateTime, ZonedDateTime endDateTime) { }
