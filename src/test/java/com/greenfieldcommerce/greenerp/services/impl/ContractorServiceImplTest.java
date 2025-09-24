package com.greenfieldcommerce.greenerp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;

@ExtendWith(MockitoExtension.class)
class ContractorServiceImplTest
{
	private static final String EMAIL = "email";
	private static final String NAME = "name";
	private static final Long INVALID_RESOURCE_ID = -1L;

	@Mock
	private ContractorRepository contractorRepository;
	@Mock
	private Mapper<CreateContractorRecord, Contractor> createContractorMapper;
	@Mock
	private Mapper<Contractor, ContractorRecord> contractorToRecordMapper;

	private ContractorServiceImpl service;

	@BeforeEach
	public void setup()
	{
		service = new ContractorServiceImpl(contractorRepository, createContractorMapper, contractorToRecordMapper);
	}

	@Test
	@DisplayName("Should find all contractors")
	void shouldFindAllContractors()
	{
		Contractor contractorA = Contractor.create(EMAIL, NAME);
		Contractor contractorB = Contractor.create(EMAIL, NAME);
		Contractor contractorC = Contractor.create(EMAIL, NAME);

		ContractorRecord contractorRecordA = new ContractorRecord(1L, EMAIL, NAME, null);
		ContractorRecord contractorRecordB = new ContractorRecord(2L, EMAIL, NAME, null);
		ContractorRecord contractorRecordC = new ContractorRecord(3L, EMAIL, NAME, null);

		when(contractorRepository.findAll()).thenReturn(List.of(contractorA, contractorB, contractorC));

		when(contractorToRecordMapper.map(contractorA)).thenReturn(contractorRecordA);
		when(contractorToRecordMapper.map(contractorB)).thenReturn(contractorRecordB);
		when(contractorToRecordMapper.map(contractorC)).thenReturn(contractorRecordC);

		final List<ContractorRecord> result = service.findAll();

		assertEquals(3, result.size());
		assertEquals(contractorRecordA, result.getFirst());
		assertEquals(contractorRecordC, result.getLast());
	}

	@Test
	@DisplayName("Should find a contractor record by id")
	void shouldFindContractorRecordById()
	{
		final long ID = 1L;
		Contractor contractor = Contractor.create(EMAIL, NAME);

		ContractorRecord record = new ContractorRecord(ID, EMAIL, NAME, null);

		when(contractorRepository.findById(ID)).thenReturn(Optional.of(contractor));
		when(contractorToRecordMapper.map(contractor)).thenReturn(record);

		final ContractorRecord result = service.findById(ID);

		assertEquals(record, result);
		verify(contractorRepository).findById(ID);
		verify(contractorToRecordMapper).map(contractor);
	}

	@Test
	@DisplayName("Should find contractor entity by id")
	void shouldFindContractorEntityById()
	{
		final long ID = 1L;
		Contractor contractor = Contractor.create(EMAIL, NAME);
		when(contractorRepository.findById(ID)).thenReturn(Optional.of(contractor));

		final Contractor result = service.findEntityById(ID);
		assertEquals(contractor, result);
		verify(contractorRepository).findById(ID);
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when cannot find a contractor by id")
	void shouldThrowEntityNotFoundExceptionWhenCannotFindAContractorById()
	{
		when(contractorRepository.findById(INVALID_RESOURCE_ID)).thenReturn(Optional.empty());
		try
		{
			service.findEntityById(INVALID_RESOURCE_ID);
			fail("Should have thrown EntityNotFoundException, as there is no contractor with id " + INVALID_RESOURCE_ID);
		}
		catch (EntityNotFoundException e)
		{
			assertEquals("ENTITY_NOT_FOUND", e.getCode());
		}
	}

	@Test
	@DisplayName("Should create a contractor with valid data")
	void shouldCreateContractorWithValidData()
	{
		CreateContractorRecord record = new CreateContractorRecord(EMAIL, NAME);

		Contractor newContractor = Contractor.create(EMAIL, NAME);
		Contractor saved = Contractor.create(EMAIL, NAME);
		final ContractorRecord expected = new ContractorRecord(saved.getId(), EMAIL, NAME, null);

		when(createContractorMapper.map(record)).thenReturn(newContractor);
		when(contractorRepository.save(newContractor)).thenReturn(saved);
		when(contractorToRecordMapper.map(saved)).thenReturn(expected);

		ContractorRecord result = service.create(record);

		assertNotNull(result);
		assertEquals(expected, result);
		verify(contractorRepository).save(newContractor);
	}

	@Test
	@DisplayName("Should throw DuplicateContractorException when saving with duplicate email")
	void shouldThrowDuplicateContractorExceptionWhenSavingWithDuplicateEmail()
	{
		CreateContractorRecord record = new CreateContractorRecord(EMAIL, NAME);
		Contractor newContractor = Contractor.create(EMAIL, NAME);
		when(createContractorMapper.map(record)).thenReturn(newContractor);
		when(contractorRepository.save(newContractor)).thenThrow(DataIntegrityViolationException.class);

		assertThrows(DuplicateContractorException.class, () -> service.create(record));
	}

	@Test
	@DisplayName("Should update a contractor with valid data")
	void shouldUpdateContractorWithValidData()
	{
		CreateContractorRecord record = new CreateContractorRecord(EMAIL, NAME);
		final long ID = 1L;
		Contractor existing = mock(Contractor.class);

		Contractor updated = Contractor.create(EMAIL, NAME);
		ContractorRecord expected = new ContractorRecord(ID, EMAIL, NAME, null);

		when(contractorRepository.findById(ID)).thenReturn(Optional.of(existing));
		when(contractorRepository.save(existing)).thenReturn(updated);
		when(contractorToRecordMapper.map(updated)).thenReturn(expected);

		ContractorRecord result = service.update(ID, record);

		verify(existing).setEmail(record.email());
		verify(existing).setName(record.name());
		verify(contractorRepository).save(existing);
		assertEquals(expected, result);
	}
}