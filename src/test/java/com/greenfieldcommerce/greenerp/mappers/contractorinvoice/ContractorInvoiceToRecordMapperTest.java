package com.greenfieldcommerce.greenerp.mappers.contractorinvoice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractorInvoiceToRecordMapperTest {

	private static final ZonedDateTime START = ZonedDateTime.now();
	private static final ZonedDateTime END = START.plusDays(30);
	private static final BigDecimal WORKED_DAYS = BigDecimal.valueOf(20);
	private static final BigDecimal EXTRA_AMOUNT = BigDecimal.valueOf(200.0);
	private static final BigDecimal TOTAL = BigDecimal.valueOf(5000.0);

    private final ContractorInvoiceToRecordMapper mapper = new ContractorInvoiceToRecordMapper();

    @Test
    @DisplayName("Should map ContractorInvoice to ContractorInvoiceRecord with valid data")
    void shouldMapContractorInvoiceToContractorInvoiceRecordWithValidData()
	{
		ContractorInvoice contractorInvoice = validInvoice();

		ContractorInvoiceRecord result = mapper.map(contractorInvoice);

		assertNotNull(result);
		assertEquals(START, result.startDate());
		assertEquals(END, result.endDate());
		assertEquals(WORKED_DAYS, result.numberOfWorkedDays());
		assertEquals(EXTRA_AMOUNT, result.extraAmount());
		assertEquals(TOTAL, result.total());
	}

	ContractorInvoice validInvoice()
	{
		ContractorInvoice invoice = mock(ContractorInvoice.class);
		when(invoice.getStartDate()).thenReturn(START);
		when(invoice.getEndDate()).thenReturn(END);
		when(invoice.getNumberOfWorkedDays()).thenReturn(WORKED_DAYS);
		when(invoice.getExtraAmount()).thenReturn(EXTRA_AMOUNT);
		when(invoice.getTotal()).thenReturn(TOTAL);

		return invoice;
	}
}
