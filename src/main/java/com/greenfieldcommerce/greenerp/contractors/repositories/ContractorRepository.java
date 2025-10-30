package com.greenfieldcommerce.greenerp.contractors.repositories;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;

@Repository
public interface ContractorRepository extends ListCrudRepository<Contractor, Long>
{
}
