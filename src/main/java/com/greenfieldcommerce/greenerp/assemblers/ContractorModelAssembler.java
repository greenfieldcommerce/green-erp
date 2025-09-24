package com.greenfieldcommerce.greenerp.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.controllers.ContractorInvoicesController;
import com.greenfieldcommerce.greenerp.controllers.ContractorRatesController;
import com.greenfieldcommerce.greenerp.controllers.ContractorsController;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;

@Component
public class ContractorModelAssembler implements RepresentationModelAssembler<ContractorRecord, EntityModel<ContractorRecord>>
{
	@Override
	public EntityModel<ContractorRecord> toModel(final ContractorRecord contractor)
	{
		return EntityModel.of(contractor,
			linkTo(methodOn(ContractorsController.class).getContractorDetails(contractor.id())).withSelfRel()
//			linkTo(methodOn(ContractorRatesController.class).findRatesForContractor(contractor.id())).withRel("rates"),
//			linkTo(methodOn(ContractorInvoicesController.class).findCurrentInvoice(contractor.id())).withRel("currentInvoice")
		);
	}
}
