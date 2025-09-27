package com.greenfieldcommerce.greenerp.records.contractorrate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonFormat;

@Relation(collectionRelation = "rates")
public record ContractorRateRecord(Long contractorId, Long id, BigDecimal rate, Currency currency, ZonedDateTime startDateTime, ZonedDateTime endDateTime) { }