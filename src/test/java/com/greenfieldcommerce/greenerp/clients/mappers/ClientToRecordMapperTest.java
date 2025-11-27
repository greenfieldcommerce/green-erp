package com.greenfieldcommerce.greenerp.clients.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientToRecordMapperTest {

    private final ClientToRecordMapper mapper = new ClientToRecordMapper();

	@Test
	@DisplayName("Should map Client to ClientRecord with valid data")
	void mapShouldReturnClientRecordWithCorrectValues()
	{
		Client client = mock(Client.class);
		when(client.getId()).thenReturn(1L);
		when(client.getName()).thenReturn("Client Name");
		when(client.getEmail()).thenReturn("client@greenfieldcommerce.com");

		ClientRecord result = mapper.map(client);

		assertEquals(client.getId(), result.id());
		assertEquals(client.getName(), result.name());
		assertEquals(client.getEmail(), result.email());
	}
}
