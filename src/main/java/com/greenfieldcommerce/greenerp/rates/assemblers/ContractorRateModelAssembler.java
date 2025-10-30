package com.greenfieldcommerce.greenerp.rates.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.rates.controllers.ContractorRatesController;
import com.greenfieldcommerce.greenerp.contractors.controllers.ContractorsController;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;

@Component
public class ContractorRateModelAssembler extends RepresentationModelAssemblerSupport<ContractorRateRecord, EntityModel<ContractorRateRecord>>
{
	public ContractorRateModelAssembler()
	{
		super(ContractorRatesController.class, (Class<EntityModel<ContractorRateRecord>>) (Class<?>)EntityModel.class);
	}

	@Override
	public EntityModel<ContractorRateRecord> toModel(final ContractorRateRecord entity)
	{
		return EntityModel.of(entity,
			linkTo(methodOn(ContractorRatesController.class).getContractorRate(entity.contractorId(), entity.id())).withSelfRel(),
			linkTo(methodOn(ContractorsController.class).getContractorDetails(entity.contractorId())).withRel("contractor"));
	}

	public CollectionModel<EntityModel<ContractorRateRecord>> toCollectionModel(Long contractorId, Iterable<? extends ContractorRateRecord> entities)
	{
		CollectionModel<EntityModel<ContractorRateRecord>> collection = super.toCollectionModel(entities);
		collection.add(linkTo(methodOn(ContractorRatesController.class).findRatesForContractor(contractorId)).withSelfRel());

		return collection;
	}
}
