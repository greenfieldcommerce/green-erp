package com.greenfieldcommerce.greenerp.clients.services;

import java.util.List;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.records.CreateClientRecord;
import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ClientService extends EntityService<Client, Long>
{
	List<ClientRecord> findAll();
	ClientRecord createClient(CreateClientRecord clientData);
	ClientRecord findById(Long id);
}
