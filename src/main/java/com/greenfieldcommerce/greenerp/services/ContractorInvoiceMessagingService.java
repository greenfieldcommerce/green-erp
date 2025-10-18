package com.greenfieldcommerce.greenerp.services;

import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

public interface ContractorInvoiceMessagingService
{
	void sendContractorInvoiceCreatedMessage(ContractorInvoiceRecord contractorInvoiceRecord);
}
