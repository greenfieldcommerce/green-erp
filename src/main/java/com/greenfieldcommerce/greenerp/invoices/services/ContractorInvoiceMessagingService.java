package com.greenfieldcommerce.greenerp.invoices.services;

import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;

public interface ContractorInvoiceMessagingService
{
	void sendContractorInvoiceCreatedMessage(ContractorInvoiceRecord contractorInvoiceRecord);
}
