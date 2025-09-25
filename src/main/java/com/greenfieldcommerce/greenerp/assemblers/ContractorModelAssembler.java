package com.greenfieldcommerce.greenerp.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.controllers.ContractorsController;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;

@Component
public class ContractorModelAssembler extends RepresentationModelAssemblerSupport<ContractorRecord, EntityModel<ContractorRecord>>
{
	public ContractorModelAssembler()
	{
		super(ContractorsController.class, (Class<EntityModel<ContractorRecord>>) (Class<?>)EntityModel.class);
	}

	@Override
	public EntityModel<ContractorRecord> toModel(final ContractorRecord contractor)
	{
		return EntityModel.of(contractor, linkTo(methodOn(ContractorsController.class).getContractorDetails(contractor.id())).withSelfRel()
			//			linkTo(methodOn(ContractorRatesController.class).findRatesForContractor(contractor.id())).withRel("rates"),
			//			linkTo(methodOn(ContractorInvoicesController.class).findCurrentInvoice(contractor.id())).withRel("currentInvoice")
		);
	}

	@Override
	public CollectionModel<EntityModel<ContractorRecord>> toCollectionModel(Iterable<? extends ContractorRecord> entities)
	{
		CollectionModel<EntityModel<ContractorRecord>> collection = super.toCollectionModel(entities);
		collection.add(linkTo(methodOn(ContractorsController.class).getAllContractors()).withSelfRel());

		return collection;
	}
}
