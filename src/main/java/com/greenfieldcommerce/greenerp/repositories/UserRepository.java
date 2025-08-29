package com.greenfieldcommerce.greenerp.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, String>
{
	Optional<User> findByUsername(String username);
}
