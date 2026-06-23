package com.greenfieldcommerce.greenerp.clients.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.ZonedDateTime;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.clients.controllers.ClientsController;
import com.greenfieldcommerce.greenerp.contractors.invoices.assemblers.ContractorInvoiceModelAssembler;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;

@Component
public class ClientContractorInvoicesModelAssembler extends RepresentationModelAssemblerSupport<ContractorInvoiceRecord, EntityModel<ContractorInvoiceRecord>>
{

	private final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler;

	public ClientContractorInvoicesModelAssembler(final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler)
	{
		super(ClientsController.class, (Class<EntityModel<ContractorInvoiceRecord>>) (Class<?>)EntityModel.class);
		this.contractorInvoiceModelAssembler = contractorInvoiceModelAssembler;
	}

	@Override
	public EntityModel<ContractorInvoiceRecord> toModel(final ContractorInvoiceRecord entity)
	{
		return contractorInvoiceModelAssembler.toModel(entity);
	}

	public CollectionModel<EntityModel<ContractorInvoiceRecord>> toCollectionModel(final Long clientId, final ZonedDateTime dateTime, Iterable<? extends ContractorInvoiceRecord> entities)
	{
		CollectionModel<EntityModel<ContractorInvoiceRecord>> collection = super.toCollectionModel(entities);
		collection.add(linkTo(methodOn(ClientsController.class).getContractorInvoicesForClient(clientId, dateTime)).withSelfRel());

		return collection;
	}
}
