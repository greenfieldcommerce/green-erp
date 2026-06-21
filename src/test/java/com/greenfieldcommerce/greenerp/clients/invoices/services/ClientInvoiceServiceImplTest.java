package com.greenfieldcommerce.greenerp.clients.invoices.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.invoices.repository.ClientInvoiceRepository;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@ExtendWith(MockitoExtension.class)
public class ClientInvoiceServiceImplTest
{
	private static final Long VALID_CLIENT_ID = 1L;

	@Mock
	private ClientInvoiceRepository clientInvoiceRepository;

	@Mock
	private ClientService clientService;

	@Mock
	private Mapper<ClientInvoice, ClientInvoiceRecord> clientInvoiceToRecordMapper;

	@InjectMocks
	private ClientInvoiceServiceImpl service;

	@Test
	@DisplayName("Should find the sorted latest x invoices for client")
	public void shouldFindTheSortedLatestXInvoicesForClient()
	{
		final Client client = mock(Client.class);
		final Sort sort = Sort.by(Sort.Direction.DESC, "invoiceDate");
		final Pageable pageable = PageRequest.of(0, 2, sort);

		final ClientInvoice invoice1 = mock(ClientInvoice.class);
		final ClientInvoice invoice2 = mock(ClientInvoice.class);

		final ClientInvoiceRecord invoice1Record = mock(ClientInvoiceRecord.class);
		final ClientInvoiceRecord invoice2Record = mock(ClientInvoiceRecord.class);

		final List<ClientInvoice> invoices = List.of(invoice1, invoice2);
		final Page<ClientInvoice> page = new PageImpl<>(invoices, pageable, invoices.size());

		when(clientInvoiceRepository.findByClient(eq(client), eq(pageable))).thenReturn(page);
		when(clientService.findEntityById(VALID_CLIENT_ID)).thenReturn(client);
		when(clientInvoiceToRecordMapper.map(eq(invoice1))).thenReturn(invoice1Record);
		when(clientInvoiceToRecordMapper.map(eq(invoice2))).thenReturn(invoice2Record);

		final Page<ClientInvoiceRecord> result = service.findClientInvoicesForClient(VALID_CLIENT_ID, pageable);
		assertEquals(2, result.getNumberOfElements());
		assertEquals(invoice1Record, result.getContent().get(0));
		assertEquals(invoice2Record, result.getContent().get(1));
		assertEquals(invoices.size(), result.getTotalElements());
		assertEquals(pageable, result.getPageable());
	}
}
