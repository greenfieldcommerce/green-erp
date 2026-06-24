package com.greenfieldcommerce.greenerp.clients.invoices.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.clients.invoices.controllers.ClientInvoicesController;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;

@Component
public class ClientInvoicesModelAssembler extends RepresentationModelAssemblerSupport<ClientInvoiceRecord, EntityModel<ClientInvoiceRecord>>
{
	public ClientInvoicesModelAssembler()
	{
		super(ClientInvoicesController.class, (Class<EntityModel<ClientInvoiceRecord>>) (Class<?>) EntityModel.class);
	}

	@Override
	public EntityModel<ClientInvoiceRecord> toModel(final ClientInvoiceRecord entity)
	{
		return EntityModel.of(entity,
			linkTo(methodOn(ClientInvoicesController.class).getClientInvoice(entity.client().id(), entity.id())).withSelfRel()
		);
	}
}
