package com.greenfieldcommerce.greenerp.services.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.services.ContractorService;

@Service
public class ContractorServiceImpl implements ContractorService
{
	private final ContractorRepository contractorRepository;
	private final Mapper<CreateContractorRecord, Contractor> createContractorMapper;
	private final Mapper<Contractor, ContractorRecord> contractorToRecordMapper;

	public ContractorServiceImpl(final ContractorRepository contractorRepository, final Mapper<CreateContractorRecord, Contractor> createContractorMapper, final Mapper<Contractor, ContractorRecord> contractorToRecordMapper)
	{
		this.contractorRepository = contractorRepository;
		this.createContractorMapper = createContractorMapper;
		this.contractorToRecordMapper = contractorToRecordMapper;
	}

	@Override
	public List<ContractorRecord> findAll()
	{
		return contractorRepository.findAll().stream().map(contractorToRecordMapper::map).toList();
	}

	@Override
	public ContractorRecord findById(final Long id)
	{
		return contractorToRecordMapper.map(internalFindById(id));
	}

	@Override
	public ContractorRecord create(final CreateContractorRecord record)
	{
		try
		{
			final Contractor contractor = createContractorMapper.map(record);
			final Contractor saved = contractorRepository.save(contractor);
			return contractorToRecordMapper.map(saved);
		} catch (DataIntegrityViolationException e)
		{
			throw new DuplicateContractorException("DUPLICATE_CONTRACTOR", String.format("Contractor with email '%s' already exists", record.email()));
		}
	}

	@Override
	public ContractorRecord update(final Long id, final CreateContractorRecord record)
	{
		final Contractor contractor = internalFindById(id);
		contractor.setEmail(record.email());
		contractor.setName(record.name());

		return contractorToRecordMapper.map(contractorRepository.save(contractor));
	}

	@Override
	public Contractor findEntityById(final Long id)
	{
		return internalFindById(id);
	}

	private Contractor internalFindById(final Long id)
	{
		return contractorRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("CONTRACTOR_NOT_FOUND", String.format("Contractor with id '%s' not found", id)));
	}
}
