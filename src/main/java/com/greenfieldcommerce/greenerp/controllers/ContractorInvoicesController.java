package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.annotations.ValidatedId;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contractors/{contractorId}/invoices")
public class ContractorInvoicesController
{
	private final ContractorInvoiceService contractorInvoiceService;

	public ContractorInvoicesController(final ContractorInvoiceService contractorInvoiceService)
	{
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@GetMapping(value = "/current")
	public ContractorInvoiceRecord findCurrentInvoice(@ValidatedId(value = "contractorId") Long contractorId)
	{
		return contractorInvoiceService.findCurrentInvoiceForContractor(contractorId);
	}

	@PostMapping
	public ContractorInvoiceRecord createInvoice(@ValidatedId(value = "contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.create(contractorId, record.numberOfWorkedDays(), record.extraAmount());
	}
}
