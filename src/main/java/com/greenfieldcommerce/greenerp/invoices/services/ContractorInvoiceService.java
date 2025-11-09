package com.greenfieldcommerce.greenerp.invoices.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ContractorInvoiceService extends EntityService<ContractorInvoice, Long>
{
	Page<ContractorInvoiceRecord> findByContractor(Long contractorId, Pageable pageable);
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord findByContractorAndId(Long contractorId, Long invoiceId);
	ContractorInvoiceRecord addExtraAmountLineToInvoice(Long contractorId, Long invoiceId, CreateInvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord patchInvoice(Long contractorId, Long invoiceId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord patchExtraAmountLine(Long contractorId, Long invoiceId, Long extraLineId, CreateInvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
}
