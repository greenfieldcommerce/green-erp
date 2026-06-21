package com.greenfieldcommerce.greenerp.helpers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;

public class ClientTestValidations
{
	public static ResultMatcher validClient(String path, ClientRecord client)
	{
		return result ->
		{
			jsonPath(path + ".id").value(client.id()).match(result);
			jsonPath(path + ".name").value(client.name()).match(result);
			jsonPath(path + ".email").value(client.email()).match(result);
		};
	}
}
