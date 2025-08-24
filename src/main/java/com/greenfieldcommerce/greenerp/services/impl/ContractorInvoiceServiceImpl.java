package com.greenfieldcommerce.greenerp.services.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.services.ContractorService;
import com.greenfieldcommerce.greenerp.services.TimeService;

@Service
public class ContractorInvoiceServiceImpl implements ContractorInvoiceService
{

	private final ContractorInvoiceRepository contractorInvoiceRepository;
	private final ContractorRepository contractorRepository;
	private final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;


	public ContractorInvoiceServiceImpl(final ContractorInvoiceRepository contractorInvoiceRepository, final ContractorRepository contractorRepository, final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper)
	{
		this.contractorInvoiceRepository = contractorInvoiceRepository;
		this.contractorRepository = contractorRepository;
		this.contractorInvoiceToRecordMapper = contractorInvoiceToRecordMapper;
	}

	@Override
	public ContractorInvoiceRecord create(final Long contractorId, final BigDecimal numberOfWorkedDays, final BigDecimal extraAmount)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		final Optional<ContractorInvoice> currentInvoiceOpt = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now());
		if (currentInvoiceOpt.isPresent()) throw new DuplicateContractorInvoiceException("DUPLICATE_INVOICE", String.format("Invoice for %s already exists in the current period", contractor.getName()));

		final ContractorRate contractorRate = contractor.getCurrentRate().orElseThrow(() -> new NoActiveContractorRateException("NO_ACTIVE_RATE", String.format("No active rate for %s", contractor.getName())));
		final ContractorInvoice invoice = ContractorInvoice.create(contractorRate, numberOfWorkedDays, extraAmount);
		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
	}

	@Override
	public ContractorInvoiceRecord findCurrentInvoiceForContractor(final Long contractorId)
	{
		final Contractor contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Contractor with id %s not found", contractorId)));
		final ContractorInvoice currentInvoice = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now()).orElseThrow(() -> new EntityNotFoundException("CURRENT_INVOICE_NOT_FOUND", String.format("No current invoice for contractor %s found", contractorId)));
		return contractorInvoiceToRecordMapper.map(currentInvoice);
	}
}
