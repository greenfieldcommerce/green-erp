package com.greenfieldcommerce.greenerp.controllers;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.services.TimeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contractors/{contractorId}/invoices")
public class ContractorInvoicesController
{

	private final ContractorInvoiceRepository contractorInvoiceRepository;
	private final ContractorRepository contractorRepository;
	private final ContractorInvoiceService contractorInvoiceService;
	private final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;

	public ContractorInvoicesController(final ContractorInvoiceRepository contractorInvoiceRepository, final ContractorRepository contractorRepository, final ContractorInvoiceService contractorInvoiceService,
		final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper)
	{
		this.contractorInvoiceRepository = contractorInvoiceRepository;
		this.contractorRepository = contractorRepository;
		this.contractorInvoiceService = contractorInvoiceService;
		this.contractorInvoiceToRecordMapper = contractorInvoiceToRecordMapper;
	}

	@GetMapping(value = "/current")
	public ContractorInvoiceRecord findCurrentInvoice(@PathVariable Long contractorId)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		final ContractorInvoice currentInvoice = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current invoice not found"));
		return contractorInvoiceToRecordMapper.map(currentInvoice);
	}

	@PostMapping
	public ContractorInvoiceRecord createInvoice(@PathVariable Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		final ContractorInvoice saved = contractorInvoiceService.create(contractor, record.numberOfWorkedDays(), record.extraAmount());
		return contractorInvoiceToRecordMapper.map(saved);
	}
}
