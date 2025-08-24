package com.greenfieldcommerce.greenerp.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.annotations.ValidatedId;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.services.ContractorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors")
public class ContractorsController
{
	private final ContractorService contractorService;

	public ContractorsController(final ContractorService contractorService)
	{
		this.contractorService = contractorService;
	}

	@GetMapping
	public List<ContractorRecord> getAllContractors()
	{
		return contractorService.findAll();
	}

	@PostMapping
	public ContractorRecord createContractor(@Valid @RequestBody CreateContractorRecord record)
	{
		return contractorService.create(record);
	}

	@PatchMapping(value = "/{contractorId}")
	public ContractorRecord updateContractor(@ValidatedId(value = "contractorId") Long contractorId, @Valid @RequestBody CreateContractorRecord record)
	{
		return contractorService.update(contractorId, record);
	}
}
