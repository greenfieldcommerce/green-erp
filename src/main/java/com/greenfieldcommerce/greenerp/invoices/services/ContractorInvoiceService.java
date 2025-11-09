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
	Page<ContractorInvoiceRecord> findByContractor(Long contractorId, Pageable pageable);
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord findByContractorAndId(Long contractorId, Long invoiceId);
	ContractorInvoiceRecord addExtraAmountLineToInvoice(Long invoiceId, InvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord patchInvoice(Long contractorId, Long invoiceId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
}
