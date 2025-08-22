package com.greenfieldcommerce.greenerp.repositories;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.entities.Client;

@Repository
public interface ClientRepository extends ListCrudRepository<Client, Long>
{ }
