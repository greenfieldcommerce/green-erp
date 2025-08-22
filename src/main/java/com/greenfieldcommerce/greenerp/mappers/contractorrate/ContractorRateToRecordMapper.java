package com.greenfieldcommerce.greenerp.mappers.contractorrate;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;

@Component
public class ContractorRateToRecordMapper implements Mapper<ContractorRate, ContractorRateRecord>
{

	@Override
	public ContractorRateRecord map(final ContractorRate contractorRate)
	{
		return new ContractorRateRecord(contractorRate.getId(), contractorRate.getRate(), contractorRate.getCurrency(), contractorRate.getStartDateTime(), contractorRate.getEndDateTime());
	}
}
