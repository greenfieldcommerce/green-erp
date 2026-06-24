package com.greenfieldcommerce.greenerp.clients.invoices.controllers;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.clients.invoices.assemblers.ClientInvoicesModelAssembler;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.records.CreateClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.services.ClientInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/clients/{clientId}/invoices", produces = MediaTypes.HAL_JSON_VALUE)
public class ClientInvoicesController
{

	private final ClientInvoiceService clientInvoiceService;
	private final ClientInvoicesModelAssembler clientInvoicesModelAssembler;
	private final PagedResourcesAssembler<ClientInvoiceRecord> pagedClientInvoiceResourcesAssembler;

	public ClientInvoicesController(final ClientInvoiceService clientInvoiceService, final ClientInvoicesModelAssembler clientInvoicesModelAssembler, final PagedResourcesAssembler<ClientInvoiceRecord> pagedClientInvoiceResourcesAssembler)
	{
		this.clientInvoiceService = clientInvoiceService;
		this.clientInvoicesModelAssembler = clientInvoicesModelAssembler;
		this.pagedClientInvoiceResourcesAssembler = pagedClientInvoiceResourcesAssembler;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public PagedModel<EntityModel<ClientInvoiceRecord>> getInvoicesForClient(@PathVariable("clientId") Long clientId,
		@PageableDefault(size = 12, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable)
	{
		final Page<ClientInvoiceRecord> invoices = clientInvoiceService.findClientInvoicesForClient(clientId, pageable);
		return pagedClientInvoiceResourcesAssembler.toModel(invoices, clientInvoicesModelAssembler);
	}

	@GetMapping(value = "/{invoiceId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public EntityModel<ClientInvoiceRecord> getClientInvoice(@PathVariable("clientId") Long clientId, @PathVariable("invoiceId") Long invoiceId)
	{
		return clientInvoicesModelAssembler.toModel(clientInvoiceService.findById(invoiceId));
	}

	@PostMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ClientInvoiceRecord createInvoice(@PathVariable("clientId") Long clientId, @Valid @RequestBody CreateClientInvoiceRecord createInvoiceRecord)
	{
		List<Long> contractorInvoiceIds = Stream.of(createInvoiceRecord.contractorInvoices().split(",")).map(Long::valueOf).toList();
		return clientInvoiceService.create(clientId, contractorInvoiceIds);
	}

}
