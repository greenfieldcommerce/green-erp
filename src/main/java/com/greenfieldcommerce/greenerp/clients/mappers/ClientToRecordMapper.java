package com.greenfieldcommerce.greenerp.clients.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@Component
public class ClientToRecordMapper implements Mapper<Client, ClientRecord>
{
	@Override
	public ClientRecord map(final Client client)
	{
		return new ClientRecord(client.getId(), client.getName(), client.getEmail());
	}
}
