package com.greenfieldcommerce.greenerp.invoices.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.invoices.entities.InvoiceExtraAmountLine;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.mappers.Mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractorInvoiceToRecordMapperTest {

	private static final ZonedDateTime START = ZonedDateTime.now();
	private static final ZonedDateTime END = START.plusDays(30);
	private static final BigDecimal WORKED_DAYS = BigDecimal.valueOf(20);
	private static final BigDecimal TOTAL = BigDecimal.valueOf(5000.0);
	private static final Long CONTRACTOR_ID = 1L;

	@Mock
	private Mapper<InvoiceExtraAmountLine, InvoiceExtraAmountLineRecord> invoiceExtraAmountLineRecordMapper;

	@InjectMocks
    private ContractorInvoiceToRecordMapper mapper;

    @Test
    @DisplayName("Should map ContractorInvoice to ContractorInvoiceRecord with valid data")
    void shouldMapContractorInvoiceToContractorInvoiceRecordWithValidData()
	{
		final ContractorInvoice contractorInvoice = validInvoice();
		final InvoiceExtraAmountLine extraAmountLine = mock(InvoiceExtraAmountLine.class);
		final InvoiceExtraAmountLineRecord extraAmountLineRecord = mock(InvoiceExtraAmountLineRecord.class);

		when(contractorInvoice.getExtraAmountLines()).thenReturn(Set.of(extraAmountLine));
		when(invoiceExtraAmountLineRecordMapper.map(eq(extraAmountLine))).thenReturn(extraAmountLineRecord);

		ContractorInvoiceRecord result = mapper.map(contractorInvoice);

		assertNotNull(result);
		assertEquals(CONTRACTOR_ID, result.contractorId());
		assertEquals(START, result.startDate());
		assertEquals(END, result.endDate());
		assertEquals(WORKED_DAYS, result.numberOfWorkedDays());
		assertEquals(1, result.extraAmountLines().size());
		assertEquals(extraAmountLineRecord, result.extraAmountLines().iterator().next());
		assertEquals(TOTAL, result.total());
	}

	ContractorInvoice validInvoice()
	{
		ContractorInvoice invoice = mock(ContractorInvoice.class);

		Contractor contractor = mock(Contractor.class);
		when(contractor.getId()).thenReturn(CONTRACTOR_ID);

		when(invoice.getContractor()).thenReturn(contractor);
		when(invoice.getStartDate()).thenReturn(START);
		when(invoice.getEndDate()).thenReturn(END);
		when(invoice.getNumberOfWorkedDays()).thenReturn(WORKED_DAYS);
		when(invoice.getTotal()).thenReturn(TOTAL);

		return invoice;
	}
}
