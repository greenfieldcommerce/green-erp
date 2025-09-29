package com.greenfieldcommerce.greenerp.services.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.services.ContractorService;

///
/// Implementation of [ContractorService] for managing contractors.
///
/// This service handles:
/// - Creation of new contractors with duplicate email validation
/// - Retrieval of contractors (all or by ID)
/// - Updating existing contractor information
///
@Service
public class ContractorServiceImpl extends BaseEntityService<Contractor, Long> implements ContractorService
{
	private final ContractorRepository contractorRepository;
	private final Mapper<CreateContractorRecord, Contractor> createContractorMapper;
	private final Mapper<Contractor, ContractorRecord> contractorToRecordMapper;

	public ContractorServiceImpl(final ContractorRepository contractorRepository, final Mapper<CreateContractorRecord, Contractor> createContractorMapper, final Mapper<Contractor, ContractorRecord> contractorToRecordMapper)
	{
		super(contractorRepository, Contractor.class);
		this.contractorRepository = contractorRepository;
		this.createContractorMapper = createContractorMapper;
		this.contractorToRecordMapper = contractorToRecordMapper;
	}

	///
	/// Retrieves all contractors in the system.
	///
	/// @return a list of `ContractorRecord` objects representing all contractors
	///
	@Override
	public List<ContractorRecord> findAll()
	{
		return contractorRepository.findAll().stream().map(contractorToRecordMapper::map).toList();
	}

	///
	/// Finds a contractor by their unique ID.
	///
	/// @param id the ID of the contractor
	/// @return a `ContractorRecord` representing the found contractor
	/// @throws com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException if the contractor with the given ID is not found
	///
	@Override
	public ContractorRecord findById(final Long id)
	{
		return contractorToRecordMapper.map(findEntityById(id));
	}

	///
	/// Creates a new contractor with the provided information.
	///
	/// This method validates that the email address is unique. If a contractor with the same
	/// email already exists, a `DuplicateContractorException` is thrown.
	///
	/// @param record the record containing contractor details (name, email, etc.)
	/// @return a `ContractorRecord` representing the created contractor
	/// @throws DuplicateContractorException if a contractor with the same email already exists
	///
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

	///
	/// Updates an existing contractor's information.
	///
	/// @param id the ID of the contractor to update
	/// @param record the record containing updated contractor details
	/// @return a `ContractorRecord` representing the updated contractor
	/// @throws com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException if the contractor with the given ID is not found
	///
	@Override
	public ContractorRecord update(final Long id, final CreateContractorRecord record)
	{
		final Contractor contractor = findEntityById(id);
		contractor.setEmail(record.email());
		contractor.setName(record.name());

		return contractorToRecordMapper.map(contractorRepository.save(contractor));
	}
}
