package com.greenfieldcommerce.greenerp.contractors.records;

import org.springframework.hateoas.server.core.Relation;

import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;

@Relation(collectionRelation = "contractors")
public record ContractorRecord(Long id, String email, String name, ContractorRateRecord currentRate)
{

}
