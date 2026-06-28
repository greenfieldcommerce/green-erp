package com.greenfieldcommerce.greenerp.clients.invoices.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

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
import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
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

	@Mock
	private ContractorInvoiceService contractorInvoiceService;

	@InjectMocks
	private ClientInvoiceServiceImpl service;

	@Test
	@DisplayName("Should find client invoice record by id")
	public void shouldFindClientInvoiceRecordById()
	{
		ClientInvoice clientInvoice = mock(ClientInvoice.class);
		ClientInvoiceRecord expected = mock(ClientInvoiceRecord.class);

		when(clientInvoiceRepository.findById(VALID_CLIENT_ID)).thenReturn(Optional.of(clientInvoice));
		when(clientInvoiceToRecordMapper.map(clientInvoice)).thenReturn(expected);

		ClientInvoiceRecord result = service.findById(VALID_CLIENT_ID);

		assertEquals(expected, result);
	}

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

	@Test
	@DisplayName("Should create client invoice")
	public void shouldCreateClientInvoice()
	{
		Long invoiceId1 = 1L;
		Long invoiceId2 = 2L;
		Integer invoiceDueGap = 10;
		Currency currency = Currency.getInstance("USD");
		Client client = mock(Client.class);
		ClientInvoice clientInvoice = mock(ClientInvoice.class);

		ContractorInvoice contractorInvoice1 = mock(ContractorInvoice.class);
		ContractorInvoice contractorInvoice2 = mock(ContractorInvoice.class);
		ClientInvoiceRecord clientInvoiceRecord = mock(ClientInvoiceRecord.class);

		when(client.getInvoiceDueDateGap()).thenReturn(invoiceDueGap);
		when(client.getInvoiceCurrency()).thenReturn(currency);
		when(clientService.findEntityById(VALID_CLIENT_ID)).thenReturn(client);
		when(contractorInvoiceService.findEntityById(eq(invoiceId1))).thenReturn(contractorInvoice1);
		when(contractorInvoiceService.findEntityById(eq(invoiceId2))).thenReturn(contractorInvoice2);
		when(contractorInvoice1.getTotal()).thenReturn(BigDecimal.valueOf(10L));
		when(contractorInvoice2.getTotal()).thenReturn(BigDecimal.valueOf(20L));

		when(clientInvoiceRepository.save(any(ClientInvoice.class))).thenReturn(clientInvoice);
		when(clientInvoiceToRecordMapper.map(eq(clientInvoice))).thenReturn(clientInvoiceRecord);

		assertEquals(clientInvoiceRecord, service.create(VALID_CLIENT_ID, List.of(invoiceId1, invoiceId2)));
	}

}
