package com.greenfieldcommerce.greenerp.services.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;

@Service
public class ContractorRateServiceImpl implements ContractorRateService
{
	private final ContractorRepository contractorRepository;
	private final ContractorRateRepository contractorRateRepository;
	private final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;

	public ContractorRateServiceImpl(final ContractorRepository contractorRepository, final ContractorRateRepository contractorRateRepository, final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper)
	{
		this.contractorRepository = contractorRepository;
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
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		final ContractorRate rate = ContractorRate.create(contractor, record.rate(), record.currency(), record.startDateTime(), record.endDateTime());
		return contractorRateToRecordMapper.map(contractorRateRepository.save(rate));
	}

	@Override
	public ContractorRateRecord changeEndDateTime(final Long contractorId, final Long rateId, final ZonedDateTime newEndDateTimeRecord)
	{
		final ContractorRate contractorRate = internalFindByIdAndContractorId(rateId, contractorId);
		contractorRate.setEndDateTime(newEndDateTimeRecord);

		return contractorRateToRecordMapper.map(contractorRateRepository.save(contractorRate));
	}

	private ContractorRate internalFindByIdAndContractorId(final Long rateId, final Long contractorId)
	{
		return contractorRateRepository.findByIdAndContractorId(rateId, contractorId).orElseThrow(() -> new EntityNotFoundException("CONTRACTOR_RATE_NOT_FOUND", String.format("Contractor rate with id '%s' not found for %s", rateId, contractorId)));
	}
}
