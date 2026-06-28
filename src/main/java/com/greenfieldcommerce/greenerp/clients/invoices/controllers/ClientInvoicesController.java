package com.greenfieldcommerce.greenerp.clients.invoices.controllers;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.records.CreateClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.services.ClientInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/clients/{clientId}/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientInvoicesController
{

	private final ClientInvoiceService clientInvoiceService;

	public ClientInvoicesController(final ClientInvoiceService clientInvoiceService)
	{
		this.clientInvoiceService = clientInvoiceService;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public Page<ClientInvoiceRecord> getInvoicesForClient(
		@PathVariable("clientId")
		Long clientId,
		@PageableDefault(size = 12, sort = "dueDate", direction = Sort.Direction.DESC)
		Pageable pageable)
	{
		return clientInvoiceService.findClientInvoicesForClient(clientId, pageable);
	}

	@GetMapping(value = "/{invoiceId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ClientInvoiceRecord getClientInvoice(
		@PathVariable("clientId")
		Long clientId,
		@PathVariable("invoiceId")
		Long invoiceId)
	{
		return clientInvoiceService.findById(invoiceId);
	}

	@PostMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ClientInvoiceRecord createInvoice(
		@PathVariable("clientId")
		Long clientId,
		@Valid
		@RequestBody
		CreateClientInvoiceRecord createInvoiceRecord)
	{
		List<Long> contractorInvoiceIds = Stream.of(createInvoiceRecord.contractorInvoices().split(",")).map(Long::valueOf).toList();
		return clientInvoiceService.create(clientId, contractorInvoiceIds);
	}

}
