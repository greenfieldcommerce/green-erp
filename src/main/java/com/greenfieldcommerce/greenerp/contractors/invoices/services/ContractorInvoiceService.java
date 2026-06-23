package com.greenfieldcommerce.greenerp.contractors.invoices.services;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.BatchContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.services.EntityService;

public interface ContractorInvoiceService extends EntityService<ContractorInvoice, Long>
{
	Page<ContractorInvoiceRecord> findByContractor(Long contractorId, Pageable pageable);
	List<ContractorInvoiceRecord> findOpenForClientBeforeDate(Long clientId, ZonedDateTime date);
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord create(BatchContractorInvoiceRecord record);
	ContractorInvoiceRecord findByContractorAndId(Long contractorId, Long invoiceId);
	ContractorInvoiceRecord addExtraAmountLineToInvoice(Long contractorId, Long invoiceId, CreateInvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord patchInvoice(Long contractorId, Long invoiceId, BigDecimal numberOfWorkedDays);
	ContractorInvoiceRecord patchExtraAmountLine(Long contractorId, Long invoiceId, Long extraLineId, CreateInvoiceExtraAmountLineRecord extraAmountLineRecord);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
}
