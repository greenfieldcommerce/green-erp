package com.greenfieldcommerce.greenerp.clients.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.clients.controllers.ClientsController;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

@Component
public class ClientModelAssembler extends RepresentationModelAssemblerSupport<ClientRecord, EntityModel<ClientRecord>>
{

	public ClientModelAssembler() {
		super(ClientsController.class, (Class<EntityModel<ClientRecord>>) (Class<?>)EntityModel.class);
	}

	@Override
	public EntityModel<ClientRecord> toModel(final ClientRecord client)
	{
		return EntityModel.of(client,
			linkTo(methodOn(ClientsController.class).getClientDetails(client.id())).withSelfRel(),
			linkTo(methodOn(ClientsController.class).getAllClients()).withRel("allClients"));
	}

	@Override
	public CollectionModel<EntityModel<ClientRecord>> toCollectionModel(Iterable<? extends ClientRecord> entities)
	{
		CollectionModel<EntityModel<ClientRecord>> collection = super.toCollectionModel(entities);
		collection.add(linkTo(methodOn(ClientsController.class).getAllClients()).withSelfRel());

		return collection;
	}
}
