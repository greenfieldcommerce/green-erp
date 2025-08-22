package com.greenfieldcommerce.greenerp.mappers.contractor;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;

@Component
public class CreateContractorMapper implements Mapper<CreateContractorRecord, Contractor>
{
	@Override
	public Contractor map(final CreateContractorRecord createContractorRecord)
	{
		final Contractor contractor = new Contractor();
		contractor.setEmail(createContractorRecord.email());
		contractor.setName(createContractorRecord.name());
		return contractor;
	}
}
