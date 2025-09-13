package com.greenfieldcommerce.greenerp.helpers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;

public class ContractorTestValidations
{
	public static ResultMatcher validContractor(String path, ContractorRecord contractor)
	{
		return result -> {
			jsonPath(path + ".id").value(contractor.id()).match(result);
			jsonPath(path + ".name").value(contractor.name()).match(result);
			jsonPath(path + ".email").value(contractor.email()).match(result);
		};
	}
}
