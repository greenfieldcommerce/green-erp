package com.greenfieldcommerce.greenerp.clients.invoices.records;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;

public record ClientInvoiceRecord(Long id,
								  ClientRecord client,
								  Currency currency,
								  ZonedDateTime invoiceDate,
								  ZonedDateTime dueDate,
								  BigDecimal total,
								  String status,
								  List<ContractorInvoiceRecord> contractorInvoices)
{
}
