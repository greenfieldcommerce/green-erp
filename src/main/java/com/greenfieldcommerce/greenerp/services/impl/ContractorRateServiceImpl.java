package com.greenfieldcommerce.greenerp.services.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.exceptions.OverlappingContractorRateException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.services.ContractorService;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;

/**
 * Implementation of {@link ContractorRateService} for managing contractor rates.
 * <p>
 * This service handles:
 * <ul>
 * <li>Creation and modification of contractor rates</li>
 * <li>Retrieval of rates for specific contractors</li>
 * <li>Validation to prevent overlapping rate periods</li>
 * <li>Finding the current active rate for a contractor</li>
 * <li>Deletion of contractor rates</li>
 * </ul>
 */
@Service
public class ContractorRateServiceImpl extends BaseEntityService<ContractorRate, Long> implements ContractorRateService
{
	private final ContractorService contractorService;
	private final ContractorRateRepository contractorRateRepository;
	private final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	public ContractorRateServiceImpl(final ContractorService contractorService, final ContractorRateRepository contractorRateRepository, final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper)
	{
		super(contractorRateRepository, ContractorRate.class);
		this.contractorService = contractorService;
		this.contractorRateRepository = contractorRateRepository;
		this.contractorRateToRecordMapper = contractorRateToRecordMapper;
	}

	/**
	 * Retrieves all rates for a specific contractor, ordered by end date (descending).
	 *
	 * @param contractorId the ID of the contractor
	 * @return a list of {@code ContractorRateRecord} objects representing the contractor's rates
	 */
	@Override
	public List<ContractorRateRecord> findRatesForContractor(final Long contractorId)
	{
		final List<ContractorRate> rates = contractorRateRepository.findByContractorIdOrderByEndDateTimeDesc(contractorId);
		return rates.stream().map(contractorRateToRecordMapper::map).toList();
	}

	/**
	 * Finds a specific contractor rate by rate ID and contractor ID.
	 *
	 * @param rateId the ID of the rate
	 * @param contractorId the ID of the contractor
	 * @return a {@code ContractorRateRecord} representing the found rate
	 * @throws EntityNotFoundException if the rate is not found for the given contractor
	 */
	@Override
	public ContractorRateRecord findByIdAndContractorId(final Long rateId, final Long contractorId)
	{
		return contractorRateToRecordMapper.map(internalFindByIdAndContractorId(rateId, contractorId));
	}

	/**
	 * Creates a new contractor rate for the specified contractor.
	 * <p>
	 * This method validates that the new rate period does not overlap with any existing
	 * rate periods for the same contractor before creating the rate.
	 *
	 * @param contractorId the ID of the contractor
	 * @param record the record containing rate details (rate amount, currency, start/end dates)
	 * @return a {@code ContractorRateRecord} representing the created rate
	 * @throws EntityNotFoundException if the contractor with the given ID is not found
	 * @throws OverlappingContractorRateException if the rate period overlaps with existing rates
	 */
	@Override
	public ContractorRateRecord create(final Long contractorId, final CreateContractorRateRecord record)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);

		validateIfNotOverlapping(contractor, record.startDateTime(), record.endDateTime(), null);
		final ContractorRate rate = ContractorRate.create(contractor, record.rate(), record.currency(), record.startDateTime(), record.endDateTime());
		return contractorRateToRecordMapper.map(contractorRateRepository.save(rate));
	}

	/**
	 * Changes the end date/time of an existing contractor rate.
	 * <p>
	 * This method validates that the modified rate period does not overlap with any other
	 * existing rate periods for the same contractor.
	 *
	 * @param contractorId the ID of the contractor
	 * @param rateId the ID of the rate to modify
	 * @param newEndDateTimeRecord the new end date/time for the rate
	 * @return a {@code ContractorRateRecord} representing the updated rate
	 * @throws EntityNotFoundException if the rate is not found for the given contractor
	 * @throws OverlappingContractorRateException if the modified period overlaps with other rates
	 */
	@Override
	public ContractorRateRecord changeEndDateTime(final Long contractorId, final Long rateId, final ZonedDateTime newEndDateTimeRecord)
	{
		final ContractorRate contractorRate = internalFindByIdAndContractorId(rateId, contractorId);
		validateIfNotOverlapping(contractorRate.getContractor(), contractorRate.getStartDateTime(), newEndDateTimeRecord, contractorRate.getId());

		contractorRate.setEndDateTime(newEndDateTimeRecord);
		return contractorRateToRecordMapper.map(contractorRateRepository.save(contractorRate));
	}

	/**
	 * Finds the current active rate for a contractor.
	 *
	 * @param contractor the contractor entity
	 * @return the current {@code ContractorRate} entity
	 * @throws NoActiveContractorRateException if no active rate exists for the contractor
	 */
	@Override
	public ContractorRate findCurrentRateForContractor(final Contractor contractor)
	{
		return contractor.getCurrentRate().orElseThrow(() -> new NoActiveContractorRateException("NO_ACTIVE_RATE", String.format("No active rate for %s", contractor.getName())));
	}

	/**
	 * Deletes a contractor rate by rate ID and contractor ID.
	 *
	 * @param contractorId the ID of the contractor
	 * @param rateId the ID of the rate to delete
	 */
	@Override
	@Transactional
	public void delete(final Long contractorId, final Long rateId)
	{
		contractorRateRepository.deleteByContractorIdAndId(contractorId, rateId);
	}

	/**
	 * Internal helper method to find a contractor rate by rate ID and contractor ID.
	 *
	 * @param rateId the ID of the rate
	 * @param contractorId the ID of the contractor
	 * @return the found {@code ContractorRate} entity
	 * @throws EntityNotFoundException if the rate is not found for the given contractor
	 */
	private ContractorRate internalFindByIdAndContractorId(final Long rateId, final Long contractorId)
	{
		return contractorRateRepository.findByIdAndContractorId(rateId, contractorId).orElseThrow(() -> new EntityNotFoundException("CONTRACTOR_RATE_NOT_FOUND", String.format("Contractor rate with id '%s' not found for %s", rateId, contractorId)));
	}

	/**
	 * Validates that a rate period does not overlap with existing rate periods for the contractor.
	 *
	 * @param contractor the contractor entity
	 * @param startDateTime the start date/time of the rate period
	 * @param endDateTime the end date/time of the rate period
	 * @param excludeId optional rate ID to exclude from the overlap check (used when updating)
	 * @throws OverlappingContractorRateException if overlapping rates are found
	 */
	private void validateIfNotOverlapping(final Contractor contractor, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime, @Nullable final Long excludeId)
	{
		final List<ContractorRate> overlapping = contractorRateRepository.findRatesForContractorIdOverlappingWithPeriod(contractor, startDateTime, endDateTime, excludeId);
		if (CollectionUtils.isNotEmpty(overlapping))
			throw new OverlappingContractorRateException("OVERLAPPING_RATE", "Overlapping contractor rates are not allowed");
	}
}
