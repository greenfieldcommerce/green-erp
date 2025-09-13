package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

	@GetMapping(value = "/current", produces = "application/json")
	@PreAuthorize("hasRole('ADMIN') or (hasRole('CONTRACTOR') and #contractorId.toString().equals(authentication.name))")
	public ContractorInvoiceRecord findCurrentInvoice(@ValidatedId(value = "contractorId") Long contractorId)
	{
		return contractorInvoiceService.findCurrentInvoiceForContractor(contractorId);
	}

	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN') or (hasRole('CONTRACTOR') and #contractorId.toString().equals(authentication.name))")
	public ContractorInvoiceRecord createInvoice(@ValidatedId(value = "contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.create(contractorId, record.numberOfWorkedDays(), record.extraAmount());
	}

	@PatchMapping(consumes = "application/json")
	@PreAuthorize("hasRole('ADMIN') or (hasRole('CONTRACTOR') and #contractorId.toString().equals(authentication.name))")
	public ContractorInvoiceRecord patchCurrentInvoice(@ValidatedId(value = "contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.patchInvoice(contractorId, record.numberOfWorkedDays(), record.extraAmount());
	}
}
