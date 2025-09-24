package com.greenfieldcommerce.greenerp.services;

import java.math.BigDecimal;

import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

public interface ContractorInvoiceService extends EntityService<ContractorInvoice, Long>
{
	ContractorInvoiceRecord create(Long contractorId, BigDecimal numberOfWorkedDays, BigDecimal extraAmount);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
	ContractorInvoiceRecord patchInvoice(Long contractorId, BigDecimal bigDecimal, BigDecimal bigDecimal1);
}
