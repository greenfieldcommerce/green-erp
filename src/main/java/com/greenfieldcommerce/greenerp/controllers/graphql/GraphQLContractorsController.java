package com.greenfieldcommerce.greenerp.controllers.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.contractors.mappers.ContractorToRecordMapper;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.repositories.ContractorRepository;

@Controller
public class GraphQLContractorsController
{

	private final ContractorRepository contractorRepository;
	private final ContractorToRecordMapper contractorToRecordMapper;

	public GraphQLContractorsController(final ContractorRepository contractorRepository, final ContractorToRecordMapper contractorToRecordMapper)
	{
		this.contractorRepository = contractorRepository;
		this.contractorToRecordMapper = contractorToRecordMapper;
	}

	@QueryMapping
	public ContractorRecord contractorById(@Argument int id) {
		Contractor contractor = contractorRepository.findById((long) id).orElse(null);
		if (contractor == null) return null;

		return contractorToRecordMapper.map(contractor);
	}
}
