package com.greenfieldcommerce.greenerp.clients.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.greenfieldcommerce.greenerp.clients.repositories.ClientRepository;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest
{
	private static final Long VALID_CLIENT_ID = 1L;

	@Mock
	private ClientRepository clientRepository;

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

}
