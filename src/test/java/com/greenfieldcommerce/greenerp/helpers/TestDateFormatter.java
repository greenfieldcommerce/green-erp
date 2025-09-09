package com.greenfieldcommerce.greenerp.helpers;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDateFormatter
{
	public static String formatDate(final ObjectMapper mapper, final ZonedDateTime dateTime) throws JsonProcessingException
	{
		return mapper.writeValueAsString(dateTime).replace("\"", "");
	}
}
