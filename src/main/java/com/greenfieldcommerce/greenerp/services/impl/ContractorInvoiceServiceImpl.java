package com.greenfieldcommerce.greenerp.services.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.services.TimeService;

@Service
public class ContractorInvoiceServiceImpl implements ContractorInvoiceService
{

	private final ContractorInvoiceRepository contractorInvoiceRepository;

	public ContractorInvoiceServiceImpl(final ContractorInvoiceRepository contractorInvoiceRepository)
	{
		this.contractorInvoiceRepository = contractorInvoiceRepository;
	}

	@Override
	public ContractorInvoice create(final Contractor contractor, final BigDecimal numberOfWorkedDays, final BigDecimal extraAmount)
	{
		final Optional<ContractorInvoice> currentInvoiceOpt = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now());
		if (currentInvoiceOpt.isPresent()) throw new DuplicateContractorInvoiceException("DUPLICATE_INVOICE", String.format("Invoice for %s already exists in the current period", contractor.getName()));

		final ContractorRate contractorRate = contractor.getCurrentRate().orElseThrow(() -> new NoActiveContractorRateException("NO_ACTIVE_RATE", String.format("No active rate for %s", contractor.getName())));
		final ContractorInvoice invoice = ContractorInvoice.create(contractorRate, numberOfWorkedDays, extraAmount);
		return contractorInvoiceRepository.save(invoice);
	}
}
