package com.greenfieldcommerce.greenerp.exceptions;

public class IllegalInvoiceModificationException extends BusinessException
{
	public IllegalInvoiceModificationException(String code, String message)
	{
		super(code, message);
	}
}
