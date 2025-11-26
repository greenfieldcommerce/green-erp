package com.greenfieldcommerce.greenerp.clients.services;

import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.repositories.ClientRepository;
import com.greenfieldcommerce.greenerp.services.BaseEntityService;

@Service
public class ClientServiceImpl extends BaseEntityService<Client, Long> implements ClientService
{
	private final ClientRepository repository;

	public ClientServiceImpl(final ClientRepository repository)
	{
		super(repository, Client.class);
		this.repository = repository;
	}
}
