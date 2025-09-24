package com.greenfieldcommerce.greenerp.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;

public interface ContractorRateService extends EntityService<ContractorRate, Long>
{
	List<ContractorRateRecord> findRatesForContractor(Long contractorId);
	ContractorRateRecord findByIdAndContractorId(Long rateId, Long contractorId);
	ContractorRateRecord create(Long contractorId, CreateContractorRateRecord record);
	ContractorRateRecord changeEndDateTime(Long contractorId, Long rateId, ZonedDateTime newEndDateTime);
	ContractorRate findCurrentRateForContractor(Contractor contractor);
	void delete(Long contractorId, Long rateId);
}
