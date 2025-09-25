package com.greenfieldcommerce.greenerp.records.contractor;

import org.springframework.hateoas.server.core.Relation;

import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;

@Relation(collectionRelation = "contractors")
public record ContractorRecord(Long id, String email, String name, ContractorRateRecord currentRate)
{

}
