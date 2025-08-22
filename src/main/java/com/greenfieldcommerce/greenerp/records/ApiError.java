package com.greenfieldcommerce.greenerp.records;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String error, String code, String message, String path, Map<String, Object> details)
{
	public ApiError(int status, String error, String code, String message, String path, Map<String, Object> details)
	{
		this(Instant.now(), status, error, code, message, path, details);
	}
}