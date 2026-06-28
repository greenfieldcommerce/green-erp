package com.greenfieldcommerce.greenerp.contractors.rates.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

public record ContractorRateRecord(Long id, Long contractorId, ClientRecord client, BigDecimal rate, Currency currency, ZonedDateTime startDateTime, ZonedDateTime endDateTime) { }
