package com.greenfieldcommerce.greenerp.contractors.rates.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greenfieldcommerce.greenerp.contractors.rates.records.ContractorRateCollectionRecord;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.contractors.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.contractors.rates.records.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.contractors.rates.services.ContractorRateService;

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
	public ContractorRateCollectionRecord findRatesForContractor(@PathVariable("contractorId") Long contractorId)
	{
		return new ContractorRateCollectionRecord(contractorRateService.findRatesForContractor(contractorId));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<ContractorRateRecord> createContractorRate(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorRateRecord record)
	{
		final ContractorRateRecord createdRate = contractorRateService.create(contractorId, record);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{rateId}").buildAndExpand(createdRate.id()).toUri();
		return ResponseEntity.created(location).body(createdRate);
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
