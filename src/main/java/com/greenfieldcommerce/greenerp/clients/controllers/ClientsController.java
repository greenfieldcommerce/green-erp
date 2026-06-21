package com.greenfieldcommerce.greenerp.clients.controllers;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.clients.assemblers.ClientModelAssembler;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

@RestController
@RequestMapping(value = "/clients", produces = MediaTypes.HAL_JSON_VALUE)
public class ClientsController
{
	private final ClientService clientService;
	private final ClientModelAssembler clientModelAssembler;

	public ClientsController(final ClientService clientService, final ClientModelAssembler clientModelAssembler)
	{
		this.clientService = clientService;
		this.clientModelAssembler = clientModelAssembler;
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
}
