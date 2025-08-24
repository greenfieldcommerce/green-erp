package com.greenfieldcommerce.greenerp.services;

import java.math.BigDecimal;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

public interface ContractorInvoiceService
{
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays, BigDecimal extraAmount);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
}
