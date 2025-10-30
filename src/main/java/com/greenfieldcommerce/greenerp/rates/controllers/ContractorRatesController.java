package com.greenfieldcommerce.greenerp.rates.controllers;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
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

import com.greenfieldcommerce.greenerp.rates.assemblers.ContractorRateModelAssembler;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.records.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.rates.services.ContractorRateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/rates", produces = MediaTypes.HAL_JSON_VALUE)
public class ContractorRatesController
{

	private final ContractorRateService contractorRateService;
	private final ContractorRateModelAssembler contractorRateModelAssembler;

	public ContractorRatesController(final ContractorRateService contractorRateService, final ContractorRateModelAssembler contractorRateModelAssembler)
	{
		this.contractorRateService = contractorRateService;
		this.contractorRateModelAssembler = contractorRateModelAssembler;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public CollectionModel<EntityModel<ContractorRateRecord>> findRatesForContractor(@PathVariable("contractorId") Long contractorId)
	{
		final List<ContractorRateRecord> ratesForContractor = contractorRateService.findRatesForContractor(contractorId);
		return contractorRateModelAssembler.toCollectionModel(contractorId, ratesForContractor);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<EntityModel<ContractorRateRecord>> createContractorRate(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorRateRecord record)
	{
		final ContractorRateRecord createdRate = contractorRateService.create(contractorId, record);
		EntityModel<ContractorRateRecord> response = contractorRateModelAssembler.toModel(createdRate);

		return ResponseEntity.created(response.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(response);
	}

	@GetMapping(value = "/{rateId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorRateRecord> getContractorRate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId)
	{
		return contractorRateModelAssembler.toModel(contractorRateService.findByIdAndContractorId(rateId, contractorId));
	}

	@PatchMapping(value = "/{rateId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public EntityModel<ContractorRateRecord> updateRateEndDate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId, @Valid @RequestBody ZonedDateTimeRecord newEndDateTimeRecord)
	{
		final ContractorRateRecord updated = contractorRateService.changeEndDateTime(contractorId, rateId, newEndDateTimeRecord.newEndDateTime());
		return contractorRateModelAssembler.toModel(updated);
	}

	@DeleteMapping(value = "/{rateId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public void deleteRate(@PathVariable("contractorId") Long contractorId, @PathVariable("rateId") Long rateId)
	{
		contractorRateService.delete(contractorId, rateId);
	}

}
