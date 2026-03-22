package com.greenfieldcommerce.greenerp.rates.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.clients.mappers.ClientToRecordMapper;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

@Component
public class ContractorRateToRecordMapper implements Mapper<ContractorRate, ContractorRateRecord>
{
	private final ClientToRecordMapper clientToRecordMapper;

	public ContractorRateToRecordMapper(final ClientToRecordMapper clientToRecordMapper)
	{
		this.clientToRecordMapper = clientToRecordMapper;
	}

	@Override
	public ContractorRateRecord map(final ContractorRate contractorRate)
	{
		final ClientRecord clientRecord = clientToRecordMapper.map(contractorRate.getClient());
		return new ContractorRateRecord(contractorRate.getId(), contractorRate.getContractor().getId(), clientRecord, contractorRate.getRate(), contractorRate.getCurrency(), contractorRate.getStartDateTime(), contractorRate.getEndDateTime());
	}
}
