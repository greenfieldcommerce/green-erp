package com.greenfieldcommerce.greenerp.clients.services;

import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.records.CreateClientRecord;
import com.greenfieldcommerce.greenerp.clients.repositories.ClientRepository;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.services.BaseEntityService;

@Service
public class ClientServiceImpl extends BaseEntityService<Client, Long> implements ClientService
{
	private final ClientRepository repository;
	private final Mapper<Client, ClientRecord> clientToRecordMapper;

	public ClientServiceImpl(final ClientRepository repository, final Mapper<Client, ClientRecord> clientToRecordMapper)
	{
		super(repository, Client.class);
		this.repository = repository;
		this.clientToRecordMapper = clientToRecordMapper;
	}

	@Override
	public ClientRecord createClient(final CreateClientRecord clientData)
	{
		final Client client = Client.create(clientData.name(), clientData.email());
		final Client created = repository.save(client);

		return clientToRecordMapper.map(created);
	}
}
