package com.greenfieldcommerce.greenerp.contractors.services;

import java.util.List;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ContractorService extends EntityService<Contractor, Long>
{
	List<ContractorRecord> findAll();
	ContractorRecord findById(Long id);
	ContractorRecord create(CreateContractorRecord record);
	ContractorRecord update(Long id, CreateContractorRecord record);
	Contractor findEntityById(Long id);
}
