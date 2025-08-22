package com.greenfieldcommerce.greenerp.mappers.contractor;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;

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
		final ContractorRateRecord current = contractor.getCurrentRate().isPresent() ? contractorRateToRecordMapper.map(contractor.getCurrentRate().get()) : null;
		return new ContractorRecord(contractor.getId(), contractor.getEmail(), contractor.getName(), current);
	}
}
