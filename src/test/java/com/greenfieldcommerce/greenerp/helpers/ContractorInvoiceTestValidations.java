package com.greenfieldcommerce.greenerp.helpers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;

public class ContractorInvoiceTestValidations
{
	public static ResultMatcher validateContractorInvoice(String path, ContractorInvoiceRecord invoice, ObjectMapper objectMapper)
	{
		return result -> {
			jsonPath(path + ".contractorId").value(invoice.contractorId()).match(result);
			jsonPath(path + ".startDate").value(TestDateFormatter.formatDate(objectMapper, invoice.startDate())).match(result);
			jsonPath(path + ".endDate").value(TestDateFormatter.formatDate(objectMapper, invoice.endDate())).match(result);
			jsonPath(path + ".numberOfWorkedDays").value(invoice.numberOfWorkedDays()).match(result);
			jsonPath(path + ".total").value(invoice.total()).match(result);
			jsonPath(path + ".extraAmount").value(invoice.extraAmount()).match(result);
			jsonPath(path + ".currency").value(invoice.currency().toString()).match(result);
		};
	}
}
