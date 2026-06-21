package com.greenfieldcommerce.greenerp.clients.invoices.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;

public interface ClientInvoiceService
{
	Page<ClientInvoiceRecord> findClientInvoicesForClient(Long clientId, Pageable pageable);
}
