package com.greenfieldcommerce.greenerp.rates.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;

@Component
public class ContractorRateToRecordMapper implements Mapper<ContractorRate, ContractorRateRecord>
{

	@Override
	public ContractorRateRecord map(final ContractorRate contractorRate)
	{
		return new ContractorRateRecord(contractorRate.getId(), contractorRate.getContractor().getId(), contractorRate.getClient().getId(), contractorRate.getRate(), contractorRate.getCurrency(), contractorRate.getStartDateTime(), contractorRate.getEndDateTime());
	}
}
