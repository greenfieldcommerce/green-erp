package com.greenfieldcommerce.greenerp.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorRate;

import jakarta.annotation.Nullable;

@Repository
public interface ContractorRateRepository extends ListCrudRepository<ContractorRate, Long>
{
	List<ContractorRate> findByContractorIdOrderByEndDateTimeDesc(final Long contractorId);
	Optional<ContractorRate> findByIdAndContractorId(final Long id, final Long contractorId);

	@Query("SELECT r FROM #{#entityName} r WHERE r.contractor = :contractor AND (:excludeId IS NULL OR r.id <> :excludeId) AND r.startDateTime <= :endDateTime AND :startDateTime <= r.endDateTime")
	List<ContractorRate> findRatesForContractorIdOverlappingWithPeriod(Contractor contractor, ZonedDateTime startDateTime, ZonedDateTime endDateTime, @Nullable Long excludeId);

	void deleteByContractorIdAndId(Long contractorId, Long id);
}

