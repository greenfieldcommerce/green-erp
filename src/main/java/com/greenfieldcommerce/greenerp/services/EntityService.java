package com.greenfieldcommerce.greenerp.services;

public interface EntityService<T, ID>
{
	boolean existsById(ID id);
	T findEntityById(ID id);
}
