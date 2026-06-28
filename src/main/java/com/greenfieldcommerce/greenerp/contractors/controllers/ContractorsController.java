package com.greenfieldcommerce.greenerp.contractors.controllers;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greenfieldcommerce.greenerp.contractors.records.ContractorCollectionRecord;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractorsController
{
	private final ContractorService contractorService;

	public ContractorsController(final ContractorService contractorService)
	{
		this.contractorService = contractorService;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ContractorCollectionRecord getAllContractors()
	{
		return new ContractorCollectionRecord(contractorService.findAll());
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<ContractorRecord> createContractor(@Valid @RequestBody CreateContractorRecord record)
	{
		final ContractorRecord newContractor = contractorService.create(record);
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{contractorId}")
			.buildAndExpand(newContractor.id()).toUri();
		return ResponseEntity.created(location).body(newContractor);
	}

	@GetMapping(value = "/{contractorId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorRecord getContractorDetails(@PathVariable("contractorId") Long contractorId)
	{
		return contractorService.findById(contractorId);
	}

	@PatchMapping(value = "/{contractorId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorRecord updateContractor(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorRecord record)
	{
		return contractorService.update(contractorId, record);
	}
}
