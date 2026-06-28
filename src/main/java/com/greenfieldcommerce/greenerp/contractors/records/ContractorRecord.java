package com.greenfieldcommerce.greenerp.contractors.records;

import com.greenfieldcommerce.greenerp.contractors.rates.records.ContractorRateRecord;

public record ContractorRecord(Long id, String email, String name, ContractorRateRecord currentRate)
{

}
