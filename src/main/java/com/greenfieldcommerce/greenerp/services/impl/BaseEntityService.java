package com.greenfieldcommerce.greenerp.services.impl;

import org.springframework.data.repository.CrudRepository;

import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.services.EntityService;

public abstract class BaseEntityService<T, ID> implements EntityService<T, ID>
{
	final CrudRepository<T, ID> repository;
	final Class<T> entityClass;

	protected BaseEntityService(final CrudRepository<T, ID> repository, final Class<T> entityClass)
	{
		this.repository = repository;
		this.entityClass = entityClass;
	}

	@Override
	public boolean existsById(final ID id)
	{
		return repository.existsById(id);
	}

	@Override
	public T findEntityById(ID id)
	{
		return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ENTITY_NOT_FOUND", String.format("%s with id '%s' not found", entityClass.toString(), id)));
	}
}
