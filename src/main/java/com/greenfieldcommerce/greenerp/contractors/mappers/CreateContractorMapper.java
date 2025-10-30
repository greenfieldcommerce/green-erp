package com.greenfieldcommerce.greenerp.contractors.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;

@Component
public class CreateContractorMapper implements Mapper<CreateContractorRecord, Contractor>
{
	@Override
	public Contractor map(final CreateContractorRecord createContractorRecord)
	{
		return Contractor.create(createContractorRecord.email(), createContractorRecord.name());
	}
}
