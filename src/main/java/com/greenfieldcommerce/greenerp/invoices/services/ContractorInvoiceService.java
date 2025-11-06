package com.greenfieldcommerce.greenerp.invoices.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ContractorInvoiceService extends EntityService<ContractorInvoice, Long>
{
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord addExtraAmountLineToInvoice(Long invoiceId, InvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
	ContractorInvoiceRecord patchInvoice(Long contractorId, BigDecimal numberOfWorkedDays);
	Page<ContractorInvoiceRecord> findByContractor(Long contractorId, Pageable pageable);
}
