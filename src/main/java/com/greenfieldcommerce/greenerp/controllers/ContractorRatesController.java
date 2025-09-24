package com.greenfieldcommerce.greenerp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/rates", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractorRatesController
{

	private final ContractorRateService contractorRateService;

	public ContractorRatesController(final ContractorRateService contractorRateService)
	{
		this.contractorRateService = contractorRateService;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public List<ContractorRateRecord> findRatesForContractor(@PathVariable("contractorId") Long contractorId)
	{
		return contractorRateService.findRatesForContractor(contractorId);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ContractorRateRecord createContractorRate(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorRateRecord record)
	{
		return contractorRateService.create(contractorId, record);
	}

	@GetMapping(value = "/{rateId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorRateRecord getContractorRate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId)
	{
		return contractorRateService.findByIdAndContractorId(rateId, contractorId);
	}

	@PatchMapping(value = "/{rateId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ContractorRateRecord updateRateEndDate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId, @Valid @RequestBody ZonedDateTimeRecord newEndDateTimeRecord)
	{
		return contractorRateService.changeEndDateTime(contractorId, rateId, newEndDateTimeRecord.newEndDateTime());
	}

	@DeleteMapping(value = "/{rateId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public void deleteRate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId)
	{
		contractorRateService.delete(contractorId, rateId);
	}

}
