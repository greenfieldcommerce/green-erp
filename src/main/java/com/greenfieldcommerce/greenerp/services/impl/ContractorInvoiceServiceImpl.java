package com.greenfieldcommerce.greenerp.services.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceMessagingService;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.services.ContractorService;
import com.greenfieldcommerce.greenerp.services.TimeService;

/**
 * Implementation of {@link ContractorInvoiceService} for managing contractor invoices.
 * <p>
 * This service handles:
 *
 * <ul>
 * <li>Creation of new contractor invoices</li>
 * <li>Retrieval of current invoices for contractors</li>
 * <li>Modification of existing invoices</li>
 * <li>Validation to prevent duplicate invoices for the same period</li>
 * <li>Calculation of invoice amounts based on current contractor rates</li>
 * </ul>
 */
@Service
public class ContractorInvoiceServiceImpl extends BaseEntityService<ContractorInvoice, Long> implements ContractorInvoiceService
{

	private final ContractorInvoiceRepository contractorInvoiceRepository;
	private final ContractorRateService contractorRateService;
	private final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;
	private final ContractorService contractorService;
	private final ContractorInvoiceMessagingService contractorInvoiceMessagingService;

	public ContractorInvoiceServiceImpl(final ContractorInvoiceRepository contractorInvoiceRepository, final ContractorRateService contractorRateService, final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper, final ContractorService contractorService,
		final ContractorInvoiceMessagingService contractorInvoiceMessagingService)
	{
		super(contractorInvoiceRepository, ContractorInvoice.class);
		this.contractorInvoiceRepository = contractorInvoiceRepository;
		this.contractorRateService = contractorRateService;
		this.contractorInvoiceToRecordMapper = contractorInvoiceToRecordMapper;
		this.contractorService = contractorService;
		this.contractorInvoiceMessagingService = contractorInvoiceMessagingService;
	}

	/**
	 * Creates a new contractor invoice for the current period.
	 * This method validates that no invoice already exists for the contractor in the current period
	 * before creating a new one. The invoice amount is calculated based on the contractor's current
	 * rate and the number of worked days, plus any additional amount.
	 *
	 * @param contractorId       the ID of the contractor for whom to create the invoice
	 * @param numberOfWorkedDays the number of days worked by the contractor
	 * @param extraAmount        the additional amount to be added to the invoice
	 * @return a {@code ContractorInvoiceRecord} representing the created invoice
	 * @throws EntityNotFoundException             if the contractor with the given ID is not found
	 * @throws DuplicateContractorInvoiceException if an invoice already exists for the contractor in the current period
	 * @throws NoActiveContractorRateException     if the contractor has no active rate
	 */
	@Override
	public ContractorInvoiceRecord create(final Long contractorId, final BigDecimal numberOfWorkedDays, final BigDecimal extraAmount)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		final Optional<ContractorInvoice> currentInvoiceOpt = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now());

		if (currentInvoiceOpt.isPresent())
			throw new DuplicateContractorInvoiceException("DUPLICATE_INVOICE", String.format("Invoice for %s already exists in the current period", contractor.getName()));

		final ContractorRate currentRateForContractor = contractorRateService.findCurrentRateForContractor(contractor);
		final ContractorInvoice invoice = ContractorInvoice.create(currentRateForContractor, numberOfWorkedDays, extraAmount);

		final ContractorInvoiceRecord createdInvoiceRecord = contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
		contractorInvoiceMessagingService.sendContractorInvoiceCreatedMessage(createdInvoiceRecord);
		return createdInvoiceRecord;
	}

	/**
	 * Retrieves the current invoice for a specific contractor.
	 *
	 * @param contractorId the ID of the contractor
	 * @return a {@code ContractorInvoiceRecord} representing the current invoice
	 * @throws EntityNotFoundException if the contractor is not found or no current invoice exists
	 */
	@Override
	public ContractorInvoiceRecord findCurrentInvoiceForContractor(final Long contractorId)
	{
		final ContractorInvoice currentInvoice = internalFindCurrentInvoiceForContractor(contractorId);
		return contractorInvoiceToRecordMapper.map(currentInvoice);
	}

	/**
	 * Updates the number of worked days and extra amount for the current invoice for a contractor.
	 * This will recalculate the invoice total.
	 *
	 * @param contractorId       the ID of the contractor
	 * @param numberOfWorkedDays the updated number of days worked
	 * @param extraAmount        the updated additional amount
	 * @return a {@code ContractorInvoiceRecord} representing the updated invoice
	 * @throws EntityNotFoundException if the contractor is not found or no current invoice exists
	 */
	@Override
	public ContractorInvoiceRecord 	patchInvoice(final Long contractorId, final BigDecimal numberOfWorkedDays, final BigDecimal extraAmount)
	{
		final ContractorInvoice currentInvoice = internalFindCurrentInvoiceForContractor(contractorId);
		currentInvoice.setNumberOfWorkedDays(numberOfWorkedDays);
		currentInvoice.setExtraAmount(extraAmount);
		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(currentInvoice));
	}

	/**
	 * Internal helper method to find the current invoice for a contractor.
	 * <p>
	 * This method retrieves the contractor entity and searches for an active invoice
	 * for the current period.
	 *
	 * @param contractorId the ID of the contractor
	 * @return the current {@code ContractorInvoice} entity
	 * @throws EntityNotFoundException if the contractor is not found or no current invoice exists
	 */
	private ContractorInvoice internalFindCurrentInvoiceForContractor(final Long contractorId)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		return contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now())
			.orElseThrow(() -> new EntityNotFoundException("CURRENT_INVOICE_NOT_FOUND", String.format("No current invoice for contractor %s found", contractorId)));
	}
}
