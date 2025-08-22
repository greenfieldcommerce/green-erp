package com.greenfieldcommerce.greenerp.repositories;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.entities.Contractor;

@Repository
public interface ContractorRepository extends ListCrudRepository<Contractor, Long>
{
}
