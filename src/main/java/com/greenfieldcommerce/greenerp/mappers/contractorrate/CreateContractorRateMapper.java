package com.greenfieldcommerce.greenerp.mappers.contractorrate;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;

@Component
public class CreateContractorRateMapper implements Mapper<CreateContractorRateRecord, ContractorRate>
{
	@Override
	public ContractorRate map(final CreateContractorRateRecord createContractorRateRecord)
	{
		final ContractorRate rate = new ContractorRate();
		rate.setRate(createContractorRateRecord.rate());
		rate.setCurrency(createContractorRateRecord.currency());
		rate.setStartDateTime(createContractorRateRecord.startDateTime());
		rate.setEndDateTime(createContractorRateRecord.endDateTime());

		return rate;
	}
}
