package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractorInvoicesController
{
	private final ContractorInvoiceService contractorInvoiceService;

	public ContractorInvoicesController(final ContractorInvoiceService contractorInvoiceService)
	{
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord createInvoice(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.create(contractorId, record.numberOfWorkedDays(), record.extraAmount());
	}

	@GetMapping(value = "/current")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord findCurrentInvoice(@PathVariable("contractorId") Long contractorId)
	{
		return contractorInvoiceService.findCurrentInvoiceForContractor(contractorId);
	}

	@PatchMapping(value = "/current", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord patchCurrentInvoice(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.patchInvoice(contractorId, record.numberOfWorkedDays(), record.extraAmount());
	}
}
