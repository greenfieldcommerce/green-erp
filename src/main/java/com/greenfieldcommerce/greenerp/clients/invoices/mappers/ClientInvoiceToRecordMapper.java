package com.greenfieldcommerce.greenerp.clients.invoices.mappers;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@Component
public class ClientInvoiceToRecordMapper implements Mapper<ClientInvoice, ClientInvoiceRecord>
{

	private final Mapper<Client, ClientRecord> clientToRecordMapper;
	private final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper;

	public ClientInvoiceToRecordMapper(final Mapper<Client, ClientRecord> clientToRecordMapper, final Mapper<ContractorInvoice, ContractorInvoiceRecord> contractorInvoiceToRecordMapper)
	{
		this.clientToRecordMapper = clientToRecordMapper;
		this.contractorInvoiceToRecordMapper = contractorInvoiceToRecordMapper;
	}

	@Override
	public ClientInvoiceRecord map(final ClientInvoice clientInvoice)
	{
		final ClientRecord clientRecord = clientToRecordMapper.map(clientInvoice.getClient());
		final List<ContractorInvoiceRecord> contractorInvoices = CollectionUtils.emptyIfNull(clientInvoice.getContractorInvoices())
			.stream().map(contractorInvoiceToRecordMapper::map).toList();

		return new ClientInvoiceRecord(
			clientInvoice.getId(),
			clientRecord,
			clientInvoice.getCurrency(),
			clientInvoice.getInvoiceDate(),
			clientInvoice.getDueDate(),
			clientInvoice.getTotal(),
			clientInvoice.getStatus().toString(),
			contractorInvoices
		);
	}
}
