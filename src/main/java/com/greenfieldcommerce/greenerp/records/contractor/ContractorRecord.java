package com.greenfieldcommerce.greenerp.records.contractor;

import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;

public record ContractorRecord(Long id, String email, String name, ContractorRateRecord currentRate)
{

}
