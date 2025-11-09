package com.greenfieldcommerce.greenerp.invoices.services;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.DuplicateContractorInvoiceException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.exceptions.NoActiveContractorRateException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.repositories.ContractorInvoiceRepository;
import com.greenfieldcommerce.greenerp.rates.services.ContractorRateService;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;
import com.greenfieldcommerce.greenerp.services.TimeService;
import com.greenfieldcommerce.greenerp.services.BaseEntityService;

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
	 * Retrieves a paginated list of invoices for a specific contractor.
	 *
	 * @param contractorId	  the ID of the contractor whose invoices are to be retrieved
	 * @param pageable        the pagination information (i.e., page number, size, and sorting)
	 * @return a {@code Page} of {@code ContractorInvoiceRecord} containing the contractor's invoices
	 * @throws EntityNotFoundException if the contractor with the given ID is not found
	 */
	@Override
	public Page<ContractorInvoiceRecord> findByContractor(final Long contractorId, final Pageable pageable)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		return contractorInvoiceRepository.findByContractor(contractor, pageable).map(contractorInvoiceToRecordMapper::map);
	}

	/**
	 * Creates a new contractor invoice for the current period.
	 * This method validates that no invoice already exists for the contractor in the current period
	 * before creating a new one. The invoice amount is calculated based on the contractor's current
	 * rate and the number of worked days, plus any additional amount.
	 *
	 * @param contractorId       the ID of the contractor for whom to create the invoice
	 * @param numberOfWorkedDays the number of days worked by the contractor
	 * @return a {@code ContractorInvoiceRecord} representing the created invoice
	 * @throws EntityNotFoundException             if the contractor with the given ID is not found
	 * @throws DuplicateContractorInvoiceException if an invoice already exists for the contractor in the current period
	 * @throws NoActiveContractorRateException     if the contractor has no active rate
	 */
	@Override
	public ContractorInvoiceRecord create(final Long contractorId, final BigDecimal numberOfWorkedDays)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		final Optional<ContractorInvoice> currentInvoiceOpt = contractorInvoiceRepository.findCurrentContractorInvoice(contractor, TimeService.now());

		if (currentInvoiceOpt.isPresent())
			throw new DuplicateContractorInvoiceException("DUPLICATE_INVOICE", String.format("Invoice for %s already exists in the current period", contractor.getName()));

		final ContractorRate currentRateForContractor = contractorRateService.findCurrentRateForContractor(contractor);
		final ContractorInvoice invoice = ContractorInvoice.create(currentRateForContractor, numberOfWorkedDays);

		final ContractorInvoiceRecord createdInvoiceRecord = contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
		contractorInvoiceMessagingService.sendContractorInvoiceCreatedMessage(createdInvoiceRecord);
		return createdInvoiceRecord;
	}

	/**
	 * Retrieves a specific invoice for a contractor by contractor ID and invoice ID.
	 *
	 * @param contractorId the ID of the contractor
	 * @param invoiceId    the ID of the invoice to retrieve
	 * @return a {@code ContractorInvoiceRecord} representing the requested invoice
	 * @throws EntityNotFoundException if the contractor is not found, or if no invoice with the given ID exists for the contractor
	 */
	@Override
	public ContractorInvoiceRecord findByContractorAndId(final Long contractorId, final Long invoiceId)
	{
		final ContractorInvoice invoice = internalFindByContractorAndId(contractorId, invoiceId);
		return contractorInvoiceToRecordMapper.map(invoice);
	}

	/**
	 * Adds an extra amount line to an existing contractor invoice and persists the change.
	 * <p>The provided {@code extraAmountLineRecord} is converted to an {@code InvoiceExtraAmountLine}
	 * associated with the invoice identified by {@code invoiceId}. The invoice is saved and mapped to a
	 * {@code ContractorInvoiceRecord} which is returned.
	 *
	 * @param contractorId          the id of the contractor to which the invoice belongs
	 * @param invoiceId             the id of the invoice to update
	 * @param extraAmountLineRecord record containing the extra amount and description
	 * @return the updated {@code ContractorInvoiceRecord}
	 * @throws EntityNotFoundException if the invoice with the given id does not exist
	 */
	@Override
	public ContractorInvoiceRecord addExtraAmountLineToInvoice(final Long contractorId, final Long invoiceId, final CreateInvoiceExtraAmountLineRecord extraAmountLineRecord)
	{
		final ContractorInvoice invoice = internalFindByContractorAndId(contractorId, invoiceId);
		final InvoiceExtraAmountLine extraAmountLine = InvoiceExtraAmountLine.create(invoice, extraAmountLineRecord.amount(), extraAmountLineRecord.description());
		invoice.addExtraAmountLine(extraAmountLine);

		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
	}

	/**
	 * Updates the number of worked days and extra amount for the current invoice for a contractor.
	 * This will recalculate the invoice total.
	 *
	 * @param contractorId       the ID of the contractor
	 * @param invoiceId          the ID of the invoice to update
	 * @param numberOfWorkedDays the updated number of days worked
	 * @return a {@code ContractorInvoiceRecord} representing the updated invoice
	 * @throws EntityNotFoundException if the contractor is not found or the invoice does not exist
	 */
	@Override
	public ContractorInvoiceRecord patchInvoice(final Long contractorId, final Long invoiceId, final BigDecimal numberOfWorkedDays)
	{
		final ContractorInvoice invoice = internalFindByContractorAndId(contractorId, invoiceId);
		invoice.setNumberOfWorkedDays(numberOfWorkedDays);
		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
	}

	/**
	 * Updates an existing extra amount line on a contractor invoice.
	 * <p>
	 * This method locates the specified extra amount line within the invoice and updates its amount
	 * and description based on the provided record. After updating the line, the total invoice amount
	 * is recalculated and the changes are persisted.
	 *
	 * @param contractorId          the ID of the contractor who owns the invoice
	 * @param invoiceId             the ID of the invoice containing the extra amount line
	 * @param extraLineId           the ID of the extra amount line to update
	 * @param extraAmountLineRecord record containing the updated extra amount and description
	 * @return a {@code ContractorInvoiceRecord} representing the updated invoice
	 * @throws EntityNotFoundException if the contractor, invoice, or extra amount line with the given ID is not found
	 */
	@Override
	public ContractorInvoiceRecord patchExtraAmountLine(final Long contractorId, final Long invoiceId, final Long extraLineId, final CreateInvoiceExtraAmountLineRecord extraAmountLineRecord)
	{
		final ContractorInvoice invoice = internalFindByContractorAndId(contractorId, invoiceId);
		final InvoiceExtraAmountLine extraAmountLine = invoice.getExtraAmountLines().stream().filter(extraLine -> extraLine.getId().equals(extraLineId)).findFirst()
			.orElseThrow(() -> new EntityNotFoundException("EXTRA_AMOUNT_LINE_NOT_FOUND", String.format("No extra amount line with id %s found for invoice %s of contractor %s", extraLineId, invoiceId, contractorId)));

		extraAmountLine.setAmount(extraAmountLineRecord.amount());
		extraAmountLine.setDescription(extraAmountLineRecord.description());

		invoice.calculateTotalInvoiceAmount();
		return contractorInvoiceToRecordMapper.map(contractorInvoiceRepository.save(invoice));
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


	/**
	 * Internal helper method to find a specific invoice for a contractor.
	 * <p>
	 * This method retrieves the contractor entity and searches for an invoice
	 * with the specified ID that belongs to that contractor.
	 *
	 * @param contractorId the ID of the contractor
	 * @param invoiceId    the ID of the invoice to find
	 * @return the {@code ContractorInvoice} entity matching the given contractor and invoice ID
	 * @throws EntityNotFoundException if the contractor is not found or no invoice with the given ID exists for the contractor
	 */
	private ContractorInvoice internalFindByContractorAndId(final Long contractorId, final Long invoiceId)
	{
		final Contractor contractor = contractorService.findEntityById(contractorId);
		return contractorInvoiceRepository.findByContractorAndId(contractor, invoiceId)
			.orElseThrow(() -> new EntityNotFoundException("INVOICE_NOT_FOUND", String.format("No invoice with id %s for contractor %s was found", invoiceId, contractorId)));
	}
}
