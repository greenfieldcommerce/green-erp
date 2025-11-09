package com.greenfieldcommerce.greenerp.invoices.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;

@Component
public class ContractorInvoiceToRecordMapper implements Mapper<ContractorInvoice, ContractorInvoiceRecord>
{
	private final Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord> invoiceExtraAmountLineRecordMapper;

	public ContractorInvoiceToRecordMapper(final Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord> invoiceExtraAmountLineRecordMapper)
	{
		this.invoiceExtraAmountLineRecordMapper = invoiceExtraAmountLineRecordMapper;
	}

	@Override
	public ContractorInvoiceRecord map(final ContractorInvoice contractorInvoice)
	{
		final Set<InvoiceExtraAmountLineRecord> extraLines = contractorInvoice.getExtraAmountLines().stream().map(invoiceExtraAmountLineRecordMapper::map).collect(Collectors.toSet());
		return new ContractorInvoiceRecord(contractorInvoice.getContractor().getId(), contractorInvoice.getId(), contractorInvoice.getStartDate(), contractorInvoice.getEndDate(), contractorInvoice.getNumberOfWorkedDays(), extraLines, contractorInvoice.getTotal(), contractorInvoice.getCurrency());
	}
}
