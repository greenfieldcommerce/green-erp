package com.greenfieldcommerce.greenerp.contractors.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;

@Component
public class ContractorToRecordMapper implements Mapper<Contractor, ContractorRecord>
{

	private final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	public ContractorToRecordMapper(final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper)
	{
		this.contractorRateToRecordMapper = contractorRateToRecordMapper;
	}

	@Override
	public ContractorRecord map(final Contractor contractor)
	{
		final ContractorRateRecord current = contractor.getCurrentRate()
		    .map(contractorRateToRecordMapper::map)
		    .orElse(null);
		return new ContractorRecord(contractor.getId(), contractor.getEmail(), contractor.getName(), current);
	}
}
