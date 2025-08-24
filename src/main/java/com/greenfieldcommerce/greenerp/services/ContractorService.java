package com.greenfieldcommerce.greenerp.services;

import java.util.List;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;

public interface ContractorService
{
	List<ContractorRecord> findAll();
	ContractorRecord findById(Long id);
	ContractorRecord create(CreateContractorRecord record);
	ContractorRecord update(Long id, CreateContractorRecord record);

	Contractor findEntityById(Long id);
}
