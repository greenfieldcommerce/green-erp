package com.greenfieldcommerce.greenerp.exceptions;

public class BusinessException extends RuntimeException
{
	private final String code;

	public BusinessException(final String code, String message)
	{
		super(message);
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}
}
