package com.greenfieldcommerce.greenerp.invoices.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greenfieldcommerce.greenerp.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

@ExtendWith(MockitoExtension.class)
public class InvoiceExtraAmountLineRecordMapperTest
{

	private final Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord> mapper = new InvoiceExtraAmountLineRecordMapper();

	@Test
	@DisplayName("Should map entity to record")
	public void shouldMapEntityToRecord()
	{
		final InvoiceExtraAmountLine line = mock(InvoiceExtraAmountLine.class);

		when(line.getAmount()).thenReturn(BigDecimal.valueOf(150D));
		when(line.getDescription()).thenReturn("Service Charge");

		final InvoiceExtraAmountLineRecord mapped = mapper.map(line);

		assertEquals(line.getAmount(), mapped.extraAmount());
		assertEquals(line.getDescription(), mapped.description());
	}
}
