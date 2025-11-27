package com.greenfieldcommerce.greenerp.clients.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.records.CreateClientRecord;
import com.greenfieldcommerce.greenerp.clients.repositories.ClientRepository;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest
{
	private static final Long VALID_CLIENT_ID = 1L;

	@Mock
	private ClientRepository clientRepository;
	@Mock
	private Mapper<Client, ClientRecord> clientToRecordMapper;


	@InjectMocks
	private ClientServiceImpl service;

	@Test
	@DisplayName("Should find client by id")
	public void shouldFindClientById()
	{
		final Client client = mock(Client.class);
		when(clientRepository.findById(VALID_CLIENT_ID)).thenReturn(Optional.of(client));

		final Client result = service.findEntityById(VALID_CLIENT_ID);

		verify(clientRepository).findById(VALID_CLIENT_ID);
		assertEquals(client, result);
	}

	@Test
	@DisplayName("Should throw EntityNotFoundException when client cannot be found")
	public void shouldThrowEntityNotFoundExceptionWhenClientNotFound()
	{
		when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());
		try
		{
			service.findEntityById(-1L);
			fail("Should have thrown EntityNotFoundException, as there is no client with id " + -1L);
		}
		catch (EntityNotFoundException e)
		{
			assertEquals("ENTITY_NOT_FOUND", e.getCode());
		}
	}

	@Test
	@DisplayName("Should create client")
	public void shouldCreateClient()
	{
		final CreateClientRecord clientRecord = new CreateClientRecord("Client Name", "client@greenfieldcommerce.com");
		final Client client = mock(Client.class);
		final ClientRecord expected = mock(ClientRecord.class);

		when(clientRepository.save(argThat(c-> c.getName().equals(clientRecord.name()) && c.getEmail().equals(clientRecord.email())))).thenReturn(client);
		when(clientToRecordMapper.map(client)).thenReturn(expected);

		final ClientRecord saved = service.createClient(clientRecord);

		assertEquals(expected.id(), saved.id());
		assertEquals(expected.name(), saved.name());
		assertEquals(expected.email(), saved.email());

		verify(clientRepository).save(argThat(c -> c.getName().equals(clientRecord.name()) && c.getEmail().equals(clientRecord.email())));
	}

}
