package com.greenfieldcommerce.greenerp.repositories;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.entities.ContractorRate;

@Repository
public interface ContractorRateRepository extends ListCrudRepository<ContractorRate, Long>
{
	Optional<ContractorRate> findByIdAndContractorId(final Long id, final Long contractorId);
}

