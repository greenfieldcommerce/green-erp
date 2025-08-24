package com.greenfieldcommerce.greenerp.exceptions;

public class EntityNotFoundException extends BusinessException
{
	public EntityNotFoundException(String code, String message)
	{
		super(code, message);
	}
}
