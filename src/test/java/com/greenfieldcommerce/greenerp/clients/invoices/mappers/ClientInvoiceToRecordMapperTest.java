package com.greenfieldcommerce.greenerp.clients.invoices.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.mappers.ClientToRecordMapper;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.contractors.invoices.mappers.ContractorInvoiceToRecordMapper;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@ExtendWith(MockitoExtension.class)
public class ClientInvoiceToRecordMapperTest
{
	private static final Long VALID_INVOICE_ID = 1L;
	private static final ZonedDateTime VALID_INVOICE_DATE = ZonedDateTime.now();
	private static final ZonedDateTime VALID_DUE_DATE = VALID_INVOICE_DATE.plusDays(30);
	private static final BigDecimal VALID_TOTAL = BigDecimal.valueOf(15000.0);
	private static final Currency VALID_CURRENCY = Currency.getInstance("USD");
	private static final ClientInvoice.ClientInvoiceStatus STATUS = ClientInvoice.ClientInvoiceStatus.OPEN;
	private static final String VALID_STATUS = STATUS.toString();

	private Mapper<Client, ClientRecord> clientToRecordMapper;
	private Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;

	private ClientInvoiceToRecordMapper mapper;

	@BeforeEach
	public void setup()
	{
		this.clientToRecordMapper = mock(ClientToRecordMapper.class);
		this.contractorInvoiceToRecordMapper = mock(ContractorInvoiceToRecordMapper.class);

		mapper = new ClientInvoiceToRecordMapper(clientToRecordMapper, contractorInvoiceToRecordMapper);
	}

	@Test
	@DisplayName("Should map ClientInvoice to ClientInvoiceRecord")
	void shouldMapClientInvoiceToClientInvoiceRecord()
	{
		final Client client = mock(Client.class);
		final ContractorInvoice contractorInvoice1 = mock(ContractorInvoice.class);
		final ContractorInvoice contractorInvoice2 = mock(ContractorInvoice.class);

		final ClientInvoice clientInvoice = validClientInvoice(client, List.of(contractorInvoice1, contractorInvoice2));

		final ClientRecord clientRecord = mock(ClientRecord.class);
		final ContractorInvoiceRecord contractorInvoiceRecord1 = mock(ContractorInvoiceRecord.class);
		final ContractorInvoiceRecord contractorInvoiceRecord2 = mock(ContractorInvoiceRecord.class);

		when(clientToRecordMapper.map(client)).thenReturn(clientRecord);
		when(contractorInvoiceToRecordMapper.map(contractorInvoice1)).thenReturn(contractorInvoiceRecord1);
		when(contractorInvoiceToRecordMapper.map(contractorInvoice2)).thenReturn(contractorInvoiceRecord2);

		final ClientInvoiceRecord result = mapper.map(clientInvoice);

		assertNotNull(result);
		assertEquals(VALID_INVOICE_ID, result.id());
		assertEquals(clientRecord, result.client());
		assertEquals(VALID_INVOICE_DATE, result.invoiceDate());
		assertEquals(VALID_DUE_DATE, result.dueDate());
		assertEquals(VALID_TOTAL, result.total());
		assertEquals(VALID_CURRENCY, result.currency());
		assertEquals(VALID_STATUS, result.status());

		assertEquals(2, result.contractorInvoices().size());
		assertEquals(List.of(contractorInvoiceRecord1, contractorInvoiceRecord2), result.contractorInvoices());
	}

	private ClientInvoice validClientInvoice(final Client client, final List<ContractorInvoice> contractorInvoices)
	{
		final ClientInvoice clientInvoice = mock(ClientInvoice.class);

		when(clientInvoice.getId()).thenReturn(VALID_INVOICE_ID);
		when(clientInvoice.getClient()).thenReturn(client);

		when(clientInvoice.getInvoiceDate()).thenReturn(VALID_INVOICE_DATE);
		when(clientInvoice.getDueDate()).thenReturn(VALID_DUE_DATE);
		when(clientInvoice.getTotal()).thenReturn(VALID_TOTAL);
		when(clientInvoice.getCurrency()).thenReturn(VALID_CURRENCY);
		when(clientInvoice.getStatus()).thenReturn(STATUS);
		when(clientInvoice.getContractorInvoices()).thenReturn(contractorInvoices);

		return clientInvoice;
	}

}