package com.greenfieldcommerce.greenerp.rates.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.records.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ContractorRateService extends EntityService<ContractorRate, Long>
{
	List<ContractorRateRecord> findRatesForContractor(Long contractorId);
	ContractorRateRecord findByIdAndContractorId(Long rateId, Long contractorId);
	ContractorRateRecord create(Long contractorId, final CreateContractorRateRecord record);
	ContractorRateRecord changeEndDateTime(Long contractorId, Long rateId, ZonedDateTime newEndDateTime);
	ContractorRate findCurrentRateForContractor(Contractor contractor);
	ContractorRate findRateForContractorActiveOnAPeriod(Contractor contractor, Client client, ZonedDateTime start, ZonedDateTime end);
	void delete(Long contractorId, Long rateId);
}
