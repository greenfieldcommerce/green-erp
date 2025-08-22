package com.greenfieldcommerce.greenerp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors")
public class ContractorsController
{

	private final ContractorRepository contractorRepository;
	private final Mapper<CreateContractorRecord, Contractor> createContractorMapper;
	private final Mapper<Contractor, ContractorRecord> contractorToRecordMapper;

	public ContractorsController(final ContractorRepository contractorRepository, final Mapper<CreateContractorRecord, Contractor> createContractorMapper, final Mapper<Contractor, ContractorRecord> contractorToRecordMapper)
	{
		this.contractorRepository = contractorRepository;
		this.createContractorMapper = createContractorMapper;
		this.contractorToRecordMapper = contractorToRecordMapper;
	}

	@GetMapping
	public List<ContractorRecord> getAllContractors()
	{
		return contractorRepository.findAll().stream().map(c -> contractorToRecordMapper.map(c)).toList();
	}

	@PostMapping
	public ContractorRecord createContractor(@Valid @RequestBody CreateContractorRecord record)
	{
		final Contractor contractor = createContractorMapper.map(record);
		final Contractor saved = contractorRepository.save(contractor);
		return contractorToRecordMapper.map(saved);
	}
}
