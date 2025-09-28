package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.assemblers.ContractorModelAssembler;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.services.ContractorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors", produces = MediaTypes.HAL_JSON_VALUE)
public class ContractorsController
{
	private final ContractorService contractorService;
	private final ContractorModelAssembler contractorModelAssembler;

	public ContractorsController(final ContractorService contractorService, final ContractorModelAssembler contractorModelAssembler)
	{
		this.contractorService = contractorService;
		this.contractorModelAssembler = contractorModelAssembler;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public CollectionModel<EntityModel<ContractorRecord>> getAllContractors()
	{
		return contractorModelAssembler.toCollectionModel(contractorService.findAll());
	}

	@GetMapping(value = "/{contractorId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorRecord> getContractorDetails(@PathVariable("contractorId") Long contractorId)
	{
		return contractorModelAssembler.toModel(contractorService.findById(contractorId));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<EntityModel<ContractorRecord>> createContractor(@Valid @RequestBody CreateContractorRecord record)
	{
		final ContractorRecord newContractor = contractorService.create(record);
		final EntityModel<ContractorRecord> response = contractorModelAssembler.toModel(newContractor);

		return ResponseEntity.created(response.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(response);
	}

	@PatchMapping(value = "/{contractorId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorRecord> updateContractor(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorRecord record)
	{
		return contractorModelAssembler.toModel(contractorService.update(contractorId, record));
	}
}
