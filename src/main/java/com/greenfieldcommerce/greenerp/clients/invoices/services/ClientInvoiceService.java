package com.greenfieldcommerce.greenerp.clients.invoices.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;
import com.greenfieldcommerce.greenerp.clients.invoices.records.ClientInvoiceRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ClientInvoiceService extends EntityService<ClientInvoice, Long>
{
	Page<ClientInvoiceRecord> findClientInvoicesForClient(Long clientId, Pageable pageable);
	ClientInvoiceRecord findById(Long id);
	ClientInvoiceRecord create(Long clientId, List<Long> contractorInvoiceIds);
}
