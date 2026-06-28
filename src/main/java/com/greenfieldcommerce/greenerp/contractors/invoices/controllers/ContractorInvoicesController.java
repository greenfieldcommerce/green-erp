package com.greenfieldcommerce.greenerp.contractors.invoices.controllers;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;

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

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public Page<ContractorInvoiceRecord> findInvoices(@PathVariable("contractorId") Long contractorId,
		@PageableDefault(size = 12, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable)
	{
		return contractorInvoiceService.findByContractor(contractorId, pageable);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ResponseEntity<ContractorInvoiceRecord> createInvoice(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		final ContractorInvoiceRecord createdInvoice = contractorInvoiceService.create(contractorId, record.numberOfWorkedDays());
		final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{invoiceId}")
			.buildAndExpand(createdInvoice.invoiceId()).toUri();
		return ResponseEntity.created(location).body(createdInvoice);
	}

	@GetMapping("/{invoiceId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord getInvoice(@PathVariable("contractorId") Long contractorId, @PathVariable("invoiceId") Long invoiceId)
	{
		return contractorInvoiceService.findByContractorAndId(contractorId, invoiceId);
	}

	@PatchMapping(value = "/{invoiceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ContractorInvoiceRecord patchInvoice(@PathVariable("contractorId") Long contractorId, @PathVariable("invoiceId") Long invoiceId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		return contractorInvoiceService.patchInvoice(contractorId, invoiceId, record.numberOfWorkedDays());
	}
}
