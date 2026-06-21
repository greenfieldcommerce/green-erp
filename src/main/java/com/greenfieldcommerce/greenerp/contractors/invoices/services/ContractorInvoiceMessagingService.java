package com.greenfieldcommerce.greenerp.contractors.invoices.services;

import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;

public interface ContractorInvoiceMessagingService
{
	void sendContractorInvoiceCreatedMessage(ContractorInvoiceRecord contractorInvoiceRecord);
}
