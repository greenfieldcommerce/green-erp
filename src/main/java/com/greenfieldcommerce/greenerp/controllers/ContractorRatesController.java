package com.greenfieldcommerce.greenerp.controllers;

import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/rates")
public class ContractorRatesController
{

	private final ContractorRepository contractorRepository;
	private final ContractorRateRepository contractorRateRepository;
	private final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper;
	private final Mapper<CreateContractorRateRecord, ContractorRate> createContractorRateMapper;

	public ContractorRatesController(final ContractorRepository contractorRepository, final ContractorRateRepository contractorRateRepository, final Mapper<ContractorRate, ContractorRateRecord> contractorRateToRecordMapper,
		final Mapper<CreateContractorRateRecord, ContractorRate> createContractorRateMapper)
	{
		this.contractorRepository = contractorRepository;
		this.contractorRateRepository = contractorRateRepository;
		this.contractorRateToRecordMapper = contractorRateToRecordMapper;
		this.createContractorRateMapper = createContractorRateMapper;
	}

	@GetMapping
	public List<ContractorRateRecord> findRatesForContractor(@PathVariable Long contractorId)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		return ListUtils.emptyIfNull(contractor.getRates()).stream().map(contractorRateToRecordMapper::map).toList();
	}

	@PostMapping
	public ContractorRateRecord createContractorRate(@Valid @RequestBody CreateContractorRateRecord record, @PathVariable Long contractorId)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));

		final ContractorRate contractorRate = createContractorRateMapper.map(record);
		contractorRate.setContractor(contractor);

		final ContractorRate saved = contractorRateRepository.save(contractorRate);
		return contractorRateToRecordMapper.map(saved);
	}

	@PatchMapping(value = "/{rateId}")
	public ContractorRateRecord updateRateEndDate(
		@NotNull @RequestBody ZonedDateTimeRecord newEndDateTimeRecord,
		@PathVariable Long contractorId,
		@PathVariable Long rateId)
	{
		final ContractorRate contractorRate = contractorRateRepository.findByIdAndContractorId(rateId, contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor rate with id %s not found under %s", rateId, contractorId)));

		contractorRate.setEndDateTime(newEndDateTimeRecord.newEndDateTime());
		final ContractorRate saved = contractorRateRepository.save(contractorRate);
		return contractorRateToRecordMapper.map(saved);
	}

}
