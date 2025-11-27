package com.greenfieldcommerce.greenerp.clients.services;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.records.CreateClientRecord;

public interface ClientService
{
	Client findEntityById(Long id);
	ClientRecord createClient(CreateClientRecord clientData);
}
