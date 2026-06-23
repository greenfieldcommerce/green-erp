package com.greenfieldcommerce.greenerp.clients.controllers;

import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.clients.assemblers.ClientContractorInvoicesModelAssembler;
import com.greenfieldcommerce.greenerp.clients.assemblers.ClientModelAssembler;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

@RestController
@RequestMapping(value = "/clients", produces = MediaTypes.HAL_JSON_VALUE)
public class ClientsController
{
	private final ClientService clientService;
	private final ClientModelAssembler clientModelAssembler;
	private final ClientContractorInvoicesModelAssembler clientContractorInvoicesModelAssember;
	private final ContractorInvoiceService contractorInvoiceService;

	public ClientsController(final ClientService clientService,
		final ClientModelAssembler clientModelAssembler,
		final ClientContractorInvoicesModelAssembler clientContractorInvoicesModelAssember,
		final ContractorInvoiceService contractorInvoiceService)
	{
		this.clientService = clientService;
		this.clientModelAssembler = clientModelAssembler;
		this.clientContractorInvoicesModelAssember = clientContractorInvoicesModelAssember;
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public CollectionModel<EntityModel<ClientRecord>> getAllClients()
	{
		return clientModelAssembler.toCollectionModel(clientService.findAll());
	}

	@GetMapping(value = "/{clientId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public EntityModel<ClientRecord> getClientDetails(@PathVariable("clientId") Long clientId)
	{
		return clientModelAssembler.toModel(clientService.findById(clientId));
	}

	@GetMapping(value = "/{clientId}/contractor-invoices")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public CollectionModel<EntityModel<ContractorInvoiceRecord>> getContractorInvoicesForClient(@PathVariable("clientId") Long clientId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDateBefore)
	{
		ZonedDateTime limiterDate = startDateBefore != null ? startDateBefore : ZonedDateTime.now();
		return clientContractorInvoicesModelAssember.toCollectionModel(clientId, limiterDate, contractorInvoiceService.findOpenForClientBeforeDate(clientId, limiterDate));
	}
}
