package com.greenfieldcommerce.greenerp.clients.invoices.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.mappers.ClientInvoiceToRecordMapper;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.repository.ClientInvoiceRepository;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@Service
public class ClientInvoiceServiceImpl implements ClientInvoiceService
{

	private final ClientInvoiceRepository clientInvoiceRepository;
	private final ClientService clientService;
	private final Mapper<ClientInvoice, ClientInvoiceRecord> clientInvoiceToRecordMapper;

	public ClientInvoiceServiceImpl(final ClientInvoiceRepository clientInvoiceRepository, final ClientService clientService, final Mapper<ClientInvoice, ClientInvoiceRecord> clientInvoiceToRecordMapper)
	{
		this.clientInvoiceRepository = clientInvoiceRepository;
		this.clientService = clientService;
		this.clientInvoiceToRecordMapper = clientInvoiceToRecordMapper;
	}

	@Override
	public Page<ClientInvoiceRecord> findClientInvoicesForClient(final Long clientId, final Pageable pageable)
	{
		final Client client = clientService.findEntityById(clientId);
		return clientInvoiceRepository.findByClient(client, pageable).map(clientInvoiceToRecordMapper::map);
	}
}
