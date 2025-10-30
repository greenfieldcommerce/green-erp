package com.greenfieldcommerce.greenerp.invoices.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;

@Component
public class ContractorInvoiceToRecordMapper implements Mapper<ContractorInvoice, ContractorInvoiceRecord>
{
	@Override
	public ContractorInvoiceRecord map(final ContractorInvoice contractorInvoice)
	{
		return new ContractorInvoiceRecord(contractorInvoice.getContractor().getId(), contractorInvoice.getStartDate(), contractorInvoice.getEndDate(), contractorInvoice.getNumberOfWorkedDays(), contractorInvoice.getExtraAmount(), contractorInvoice.getTotal(), contractorInvoice.getCurrency());
	}
}
