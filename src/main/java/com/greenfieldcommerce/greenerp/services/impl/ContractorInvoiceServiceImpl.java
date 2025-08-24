package com.greenfieldcommerce.greenerp.services.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.services.ContractorService;
import com.greenfieldcommerce.greenerp.services.TimeService;

@Service
public class ContractorInvoiceServiceImpl implements ContractorInvoiceService
{

	private final ContractorInvoiceRepository contractorInvoiceRepository;
	private final ContractorRateService contractorRateService;
	private final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;
	private final ContractorService contractorService;

	public ContractorInvoiceServiceImpl(final ContractorInvoiceRepository contractorInvoiceRepository, final ContractorRateService contractorRateService, final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper, final ContractorService contractorService)
	{
		this.contractorInvoiceRepository = contractorInvoiceRepository;
		this.contractorRateService = contractorRateService;
		this.contractorInvoiceToRecordMapper = contractorInvoiceToRecordMapper;
		this.contractorService = contractorService;
	}

	@Override
	public ContractorInvoiceRecord create(final Long contractorId, final BigDecimal numberOfWorkedDays, final BigDecimal extraAmount)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		final Optional<ContractorInvoice> currentInvoiceOpt = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now());

		if (currentInvoiceOpt.isPresent()) throw new DuplicateContractorInvoiceException("DUPLICATE_INVOICE", String.format("Invoice for %s already exists in the current period", contractor.getName()));

		final ContractorRate currentRateForContractor = contractorRateService.findCurrentRateForContractor(contractor);
		final ContractorInvoice invoice = ContractorInvoice.create(currentRateForContractor, numberOfWorkedDays, extraAmount);

		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
	}

	@Override
	public ContractorInvoiceRecord findCurrentInvoiceForContractor(final Long contractorId)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		final ContractorInvoice currentInvoice = internalFindCurrentInvoiceForContractor(contractorId, contractor);
		return contractorInvoiceToRecordMapper.map(currentInvoice);
	}

	private ContractorInvoice internalFindCurrentInvoiceForContractor(final Long contractorId, final Contractor contractor)
	{
		return contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now())
			.orElseThrow(() -> new EntityNotFoundException("CURRENT_INVOICE_NOT_FOUND", String.format("No current invoice for contractor %s found", contractorId)));
	}
}
