package com.greenfieldcommerce.greenerp.invoices.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;

public interface ContractorInvoiceRepository extends ListCrudRepository<ContractorInvoice, Long>
{
	@Query("SELECT i FROM #{#entityName} i WHERE i.contractor = :contractor and i.startDate <= :now AND i.endDate >= :now")
	Optional<ContractorInvoice> findCurrentContractorInvoice(Contractor contractor, ZonedDateTime now);
	Page<ContractorInvoice> findByContractor(Contractor contractor, Pageable pageable);
	Optional<ContractorInvoice> findByContractorAndId(Contractor contractor, Long id);
}
