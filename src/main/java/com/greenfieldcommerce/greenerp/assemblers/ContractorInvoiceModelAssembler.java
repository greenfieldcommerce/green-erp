package com.greenfieldcommerce.greenerp.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.controllers.ContractorInvoicesController;
import com.greenfieldcommerce.greenerp.controllers.ContractorsController;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

@Component
public class ContractorInvoiceModelAssembler implements RepresentationModelAssembler<ContractorInvoiceRecord, EntityModel<ContractorInvoiceRecord>>
{

	@Override
	public EntityModel<ContractorInvoiceRecord> toModel(ContractorInvoiceRecord invoice)
	{
		return EntityModel.of(invoice,
			linkTo(methodOn(ContractorInvoicesController.class).findCurrentInvoice(invoice.contractorId())).withSelfRel(),
			linkTo(methodOn(ContractorsController.class).getContractorDetails(invoice.contractorId())).withRel("contractor"));
	}
}