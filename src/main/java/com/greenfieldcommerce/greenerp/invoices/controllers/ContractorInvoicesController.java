package com.greenfieldcommerce.greenerp.invoices.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.invoices.assemblers.ContractorInvoiceModelAssembler;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;
import com.greenfieldcommerce.greenerp.invoices.services.ContractorInvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/contractors/{contractorId}/invoices", produces = MediaTypes.HAL_JSON_VALUE)
public class ContractorInvoicesController
{
	private final ContractorInvoiceService contractorInvoiceService;
	private final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler;
	private final PagedResourcesAssembler<ContractorInvoiceRecord> pagedContractorInvoiceResourcesAssembler;

	public ContractorInvoicesController(final ContractorInvoiceService contractorInvoiceService, final ContractorInvoiceModelAssembler contractorInvoiceModelAssembler,
		final PagedResourcesAssembler<ContractorInvoiceRecord> pagedContractorInvoiceResourcesAssembler)
	{
		this.contractorInvoiceService = contractorInvoiceService;
		this.contractorInvoiceModelAssembler = contractorInvoiceModelAssembler;
		this.pagedContractorInvoiceResourcesAssembler = pagedContractorInvoiceResourcesAssembler;
	}

	@GetMapping
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public PagedModel<EntityModel<ContractorInvoiceRecord>> findInvoices(@PathVariable("contractorId") Long contractorId,
		@PageableDefault(size = 12, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable)
	{
		final Page<ContractorInvoiceRecord> page = contractorInvoiceService.findByContractor(contractorId, pageable);
		return pagedContractorInvoiceResourcesAssembler.toModel(page, contractorInvoiceModelAssembler);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public ResponseEntity<EntityModel<ContractorInvoiceRecord>> createInvoice(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		final ContractorInvoiceRecord createdInvoice = contractorInvoiceService.create(contractorId, record.numberOfWorkedDays());
		final EntityModel<ContractorInvoiceRecord> response = contractorInvoiceModelAssembler.toModel(createdInvoice);

		return ResponseEntity.created(response.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(response);
	}

	@GetMapping(value = "/current")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorInvoiceRecord> findCurrentInvoice(@PathVariable("contractorId") Long contractorId)
	{
		final ContractorInvoiceRecord currentInvoiceForContractor = contractorInvoiceService.findCurrentInvoiceForContractor(contractorId);
		return contractorInvoiceModelAssembler.toModel(currentInvoiceForContractor);
	}

	@PatchMapping(value = "/current", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_OR_OWN_CONTRACTOR)
	public EntityModel<ContractorInvoiceRecord> patchCurrentInvoice(@PathVariable("contractorId") Long contractorId, @Valid @RequestBody CreateContractorInvoiceRecord record)
	{
		final ContractorInvoiceRecord updated = contractorInvoiceService.patchInvoice(contractorId, record.numberOfWorkedDays());
		return contractorInvoiceModelAssembler.toModel(updated);
	}

}
