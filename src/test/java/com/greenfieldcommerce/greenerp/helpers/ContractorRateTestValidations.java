package com.greenfieldcommerce.greenerp.helpers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;

public class ContractorRateTestValidations
{
	public static ResultMatcher validContractorRate(String path, ContractorRateRecord rate, ObjectMapper objectMapper)
	{
		return result -> {
			jsonPath(path + ".contractorId").value(rate.contractorId()).match(result);
			jsonPath(path + ".id").value(rate.id()).match(result);
			jsonPath(path + ".rate").value(rate.rate()).match(result);
			jsonPath(path + ".currency").value(rate.currency().toString()).match(result);
			jsonPath(path + ".startDateTime").value(TestDateFormatter.formatDate(objectMapper, rate.startDateTime())).match(result);
			jsonPath(path + ".endDateTime").value(TestDateFormatter.formatDate(objectMapper, rate.endDateTime())).match(result);
		};
	}

	public static ResultMatcher emptyContractorRate(String path)
	{
		return result -> jsonPath(path).doesNotExist().match(result);
	}
}
