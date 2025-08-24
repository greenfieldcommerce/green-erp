package com.greenfieldcommerce.greenerp.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;

public interface ContractorRateService
{
	List<ContractorRateRecord> findRatesForContractor(Long contractorId);
	ContractorRateRecord findByIdAndContractorId(Long rateId, Long contractorId);
	ContractorRateRecord create(Long contractorId, CreateContractorRateRecord record);
	ContractorRateRecord changeEndDateTime(Long contractorId, Long rateId, ZonedDateTime newEndDateTime);
}
