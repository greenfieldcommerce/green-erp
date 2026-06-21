package com.greenfieldcommerce.greenerp.contractors.invoices.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.contractors.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@Component
public class InvoiceExtraAmountLineRecordMapper implements Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord>
{
	@Override
	public InvoiceExtraAmountLineRecord map(final InvoiceExtraAmountLine extraAmountLine)
	{
		return new InvoiceExtraAmountLineRecord(extraAmountLine.getId(), extraAmountLine.getAmount(), extraAmountLine.getDescription());
	}
}
