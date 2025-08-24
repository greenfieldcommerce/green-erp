package com.greenfieldcommerce.greenerp.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.annotations.ValidatedId;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/rates")
public class ContractorRatesController
{

	private final ContractorRateService contractorRateService;

	public ContractorRatesController(final ContractorRateService contractorRateService)
	{
		this.contractorRateService = contractorRateService;
	}

	@GetMapping
	public List<ContractorRateRecord> findRatesForContractor(@ValidatedId(value = "contractorId") Long contractorId)
	{
		return contractorRateService.findRatesForContractor(contractorId);
	}

	@PostMapping
	public ContractorRateRecord createContractorRate(@ValidatedId(value = "contractorId") Long contractorId, @Valid @RequestBody CreateContractorRateRecord record)
	{
		return contractorRateService.create(contractorId, record);
	}

	@PatchMapping(value = "/{rateId}")
	public ContractorRateRecord updateRateEndDate(@ValidatedId(value = "contractorId") Long contractorId, @ValidatedId(value = "rateId") Long rateId, @NotNull @RequestBody ZonedDateTimeRecord newEndDateTimeRecord)
	{
		return contractorRateService.changeEndDateTime(contractorId, rateId, newEndDateTimeRecord.newEndDateTime());
	}

}
