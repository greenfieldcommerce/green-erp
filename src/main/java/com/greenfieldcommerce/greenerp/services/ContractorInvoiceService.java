package com.greenfieldcommerce.greenerp.services;

import java.math.BigDecimal;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;

public interface ContractorInvoiceService
{
	ContractorInvoice create(Contractor contractor, BigDecimal numberOfWorkedDays, BigDecimal extraAmount);
}
