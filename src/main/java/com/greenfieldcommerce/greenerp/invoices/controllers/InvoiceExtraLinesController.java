package com.greenfieldcommerce.greenerp.invoices.controllers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
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

import com.greenfieldcommerce.greenerp.invoices.assemblers.ContractorInvoiceModelAssembler;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/invoices/{invoiceId}/extra-lines", produces = MediaTypes.HAL_JSON_VALUE)
public class InvoiceExtraLinesController
{

	private final ContractorInvoiceService contractorInvoiceService;
	private final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler;

	public InvoiceExtraLinesController(final ContractorInvoiceService contractorInvoiceService, final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler)
	{
		this.contractorInvoiceService = contractorInvoiceService;
		this.contractorInvoiceModelAssembler = contractorInvoiceModelAssembler;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	@ResponseStatus(value = HttpStatus.CREATED)
	public EntityModel<ContractorInvoiceRecord> addExtraLine(@PathVariable Long contractorId, @PathVariable Long invoiceId, @Valid @RequestBody CreateInvoiceExtraAmountLineRecord record)
	{
		final ContractorInvoiceRecord invoice = contractorInvoiceService.addExtraAmountLineToInvoice(contractorId, invoiceId, record);
		return contractorInvoiceModelAssembler.toModel(invoice);
	}

	@PatchMapping(value = "/{extraLineId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorInvoiceRecord> updateExtraLine(@PathVariable Long contractorId, @PathVariable Long invoiceId, @PathVariable Long extraLineId, @Valid @RequestBody CreateInvoiceExtraAmountLineRecord record)
	{
		final ContractorInvoiceRecord invoice = contractorInvoiceService.patchExtraAmountLine(contractorId, invoiceId, extraLineId, record);
		return contractorInvoiceModelAssembler.toModel(invoice);
	}
}
