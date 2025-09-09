package com.greenfieldcommerce.greenerp.exceptions;

public class DuplicateContractorException extends BusinessException
{
	public DuplicateContractorException(final String code, final String message)
	{
		super(code, message);
	}
}
