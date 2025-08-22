package com.greenfieldcommerce.greenerp.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientInvoice
{

	private final Long id;
	private final Client client;
	private final List<ContractorInvoice> contractorInvoices;

	private ZonedDateTime startDate;
	private ZonedDateTime endDate;

	public ClientInvoice(final Long id, final Client client)
	{
		this.id = id;
		this.client = client;
		this.contractorInvoices = new ArrayList<>();
	}

	public void addContractorInvoice(ContractorInvoice contractorInvoice)
	{
		this.contractorInvoices.add(contractorInvoice);
	}
}
