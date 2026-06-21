package com.greenfieldcommerce.greenerp.clients.invoices.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.clients.invoices.entities.ClientInvoice;

@Repository
public interface ClientInvoiceRepository extends ListCrudRepository<ClientInvoice, Long>
{
	Page<ClientInvoice> findByClient(Client client, Pageable pageable);
}
