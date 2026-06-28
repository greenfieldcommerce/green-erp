package com.greenfieldcommerce.greenerp.clients.controllers;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.clients.records.ClientCollectionRecord;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceCollectionRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

@RestController
@RequestMapping(value = "/clients", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientsController
{
	private final ClientService clientService;
	private final ContractorInvoiceService contractorInvoiceService;

	public ClientsController(final ClientService clientService,
		final ContractorInvoiceService contractorInvoiceService)
	{
		this.clientService = clientService;
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ClientCollectionRecord getAllClients()
	{
		return new ClientCollectionRecord(clientService.findAll());
	}

	@GetMapping(value = "/{clientId}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ClientRecord getClientDetails(@PathVariable("clientId") Long clientId)
	{
		return clientService.findById(clientId);
	}

	@GetMapping(value = "/{clientId}/contractor-invoices")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ContractorInvoiceCollectionRecord getContractorInvoicesForClient(@PathVariable("clientId") Long clientId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDateBefore)
	{
		ZonedDateTime limiterDate = startDateBefore != null ? startDateBefore : ZonedDateTime.now();
		return new ContractorInvoiceCollectionRecord(contractorInvoiceService.findOpenForClientBeforeDate(clientId, limiterDate));
	}

}
