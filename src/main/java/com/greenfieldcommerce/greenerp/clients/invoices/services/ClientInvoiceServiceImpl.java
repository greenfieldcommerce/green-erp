package com.greenfieldcommerce.greenerp.clients.invoices.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.repository.ClientInvoiceRepository;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.services.BaseEntityService;

@Service
public class ClientInvoiceServiceImpl extends BaseEntityService<ClientInvoice, Long> implements ClientInvoiceService
{

	private final ClientInvoiceRepository clientInvoiceRepository;
	private final ClientService clientService;
	private final Mapper<ClientInvoice, ClientInvoiceRecord> clientInvoiceToRecordMapper;
	private final ContractorInvoiceService contractorInvoiceService;

	public ClientInvoiceServiceImpl(final ClientInvoiceRepository clientInvoiceRepository,
		final ClientService clientService, final Mapper<ClientInvoice, ClientInvoiceRecord> clientInvoiceToRecordMapper,
		final ContractorInvoiceService contractorInvoiceService)
	{
		super(clientInvoiceRepository, ClientInvoice.class);
		this.clientInvoiceRepository = clientInvoiceRepository;
		this.clientService = clientService;
		this.clientInvoiceToRecordMapper = clientInvoiceToRecordMapper;
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@Override
	public ClientInvoiceRecord findById(final Long id)
	{
		return clientInvoiceToRecordMapper.map(findEntityById(id));
	}

	@Override
	public Page<ClientInvoiceRecord> findClientInvoicesForClient(final Long clientId, final Pageable pageable)
	{
		final Client client = clientService.findEntityById(clientId);
		return clientInvoiceRepository.findByClient(client, pageable).map(clientInvoiceToRecordMapper::map);
	}

	@Override
	public ClientInvoiceRecord create(final Long clientId, final List<Long> contractorInvoiceIds)
	{
		Client client = clientService.findEntityById(clientId);
		List<ContractorInvoice> contractorInvoices = contractorInvoiceIds.stream()
			.map(contractorInvoiceService::findEntityById).toList();

		ClientInvoice invoice = ClientInvoice.create(client, contractorInvoices);

		return clientInvoiceToRecordMapper.map(clientInvoiceRepository.save(invoice));
	}
}
