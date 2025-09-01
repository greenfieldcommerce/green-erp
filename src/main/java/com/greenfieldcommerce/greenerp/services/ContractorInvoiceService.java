package com.greenfieldcommerce.greenerp.services;

import java.math.BigDecimal;

import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

import jakarta.validation.constraints.NotNull;

public interface ContractorInvoiceService
{
	ContractorInvoiceRecord create(Long contractorId, @NotNull BigDecimal numberOfWorkedDays, @NotNull BigDecimal extraAmount);
	ContractorInvoiceRecord findCurrentInvoiceForContractor(Long contractorId);
	ContractorInvoiceRecord patchInvoice(Long contractorId, @NotNull BigDecimal bigDecimal, @NotNull BigDecimal bigDecimal1);
}
