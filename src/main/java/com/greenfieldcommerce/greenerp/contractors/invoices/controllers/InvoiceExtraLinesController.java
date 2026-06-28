package com.greenfieldcommerce.greenerp.contractors.invoices.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/invoices/{invoiceId}/extra-lines", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvoiceExtraLinesController
{

	private final ContractorInvoiceService contractorInvoiceService;

	public InvoiceExtraLinesController(final ContractorInvoiceService contractorInvoiceService)
	{
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	@ResponseStatus(value = HttpStatus.CREATED)
	public ContractorInvoiceRecord addExtraLine(@PathVariable Long contractorId, @PathVariable Long invoiceId, @Valid @RequestBody CreateInvoiceExtraAmountLineRecord record)
	{
		return contractorInvoiceService.addExtraAmountLineToInvoice(contractorId, invoiceId, record);
	}

	@PatchMapping(value = "/{extraLineId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord updateExtraLine(@PathVariable Long contractorId, @PathVariable Long invoiceId, @PathVariable Long extraLineId, @Valid @RequestBody CreateInvoiceExtraAmountLineRecord record)
	{
		return contractorInvoiceService.patchExtraAmountLine(contractorId, invoiceId, extraLineId, record);
	}

}
