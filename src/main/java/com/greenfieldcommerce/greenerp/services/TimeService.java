package com.greenfieldcommerce.greenerp.services;

import java.time.ZonedDateTime;

public interface TimeService
{
	static ZonedDateTime now()
	{
		return ZonedDateTime.now();
	}
}
