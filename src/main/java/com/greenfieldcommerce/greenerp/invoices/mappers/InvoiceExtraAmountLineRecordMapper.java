package com.greenfieldcommerce.greenerp.invoices.mappers;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@Component
public class InvoiceExtraAmountLineRecordMapper implements Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord>
{
	@Override
	public InvoiceExtraAmountLineRecord map(final InvoiceExtraAmountLine extraAmountLine)
	{
		return new InvoiceExtraAmountLineRecord(extraAmountLine.getAmount(), extraAmountLine.getDescription());
	}
}
