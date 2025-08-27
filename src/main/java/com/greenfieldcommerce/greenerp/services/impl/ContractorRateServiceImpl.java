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

@Service
public class ContractorRateServiceImpl implements ContractorRateService
{
	private final ContractorService contractorService;
	private final ContractorRateRepository contractorRateRepository;
	private final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	public ContractorRateServiceImpl(final ContractorService contractorService, final ContractorRateRepository contractorRateRepository, final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper)
	{
		this.contractorService = contractorService;
		this.contractorRateRepository = contractorRateRepository;
		this.contractorRateToRecordMapper = contractorRateToRecordMapper;
	}

	@Override
	public List<ContractorRateRecord> findRatesForContractor(final Long contractorId)
	{
		final List<ContractorRate> rates = contractorRateRepository.findByContractorIdOrderByEndDateTimeDesc(contractorId);
		return rates.stream().map(contractorRateToRecordMapper::map).toList();
	}

	@Override
	public ContractorRateRecord findByIdAndContractorId(final Long rateId, final Long contractorId)
	{
		return contractorRateToRecordMapper.map(internalFindByIdAndContractorId(rateId, contractorId));
	}

	@Override
	public ContractorRateRecord create(final Long contractorId, final CreateContractorRateRecord record)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);

		validateIfNotOverlapping(contractor, record.startDateTime(), record.endDateTime(), null);
		final ContractorRate rate = ContractorRate.create(contractor, record.rate(), record.currency(), record.startDateTime(), record.endDateTime());
		return contractorRateToRecordMapper.map(contractorRateRepository.save(rate));
	}

	@Override
	public ContractorRateRecord changeEndDateTime(final Long contractorId, final Long rateId, final ZonedDateTime newEndDateTimeRecord)
	{
		final ContractorRate contractorRate = internalFindByIdAndContractorId(rateId, contractorId);
		validateIfNotOverlapping(contractorRate.getContractor(), contractorRate.getStartDateTime(), newEndDateTimeRecord, rateId);

		contractorRate.setEndDateTime(newEndDateTimeRecord);
		return contractorRateToRecordMapper.map(contractorRateRepository.save(contractorRate));
	}

	@Override
	public ContractorRate findCurrentRateForContractor(final Contractor contractor)
	{
		return contractor.getCurrentRate().orElseThrow(() -> new NoActiveContractorRateException("NO_ACTIVE_RATE", String.format("No active rate for %s", contractor.getName())));
	}

	@Override
	@Transactional
	public void delete(final Long contractorId, final Long rateId)
	{
		contractorRateRepository.deleteByContractorIdAndId(contractorId, rateId);
	}

	private ContractorRate internalFindByIdAndContractorId(final Long rateId, final Long contractorId)
	{
		return contractorRateRepository.findByIdAndContractorId(rateId, contractorId).orElseThrow(() -> new EntityNotFoundException("CONTRACTOR_RATE_NOT_FOUND", String.format("Contractor rate with id '%s' not found for %s", rateId, contractorId)));
	}

	private void validateIfNotOverlapping(final Contractor contractor, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime, @Nullable final Long excludeId)
	{
		final List<ContractorRate> overlapping = contractorRateRepository.findRatesForContractorIdOverlappingWithPeriod(contractor, startDateTime, endDateTime, excludeId);
		if (CollectionUtils.isNotEmpty(overlapping))
			throw new OverlappingContractorRateException("OVERLAPPING_RATE", "Overlapping contractor rates are not allowed");
	}
}
