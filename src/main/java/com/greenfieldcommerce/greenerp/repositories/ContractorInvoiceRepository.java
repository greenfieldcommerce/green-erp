package com.greenfieldcommerce.greenerp.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.entities.ContractorInvoice;

public interface ContractorInvoiceRepository extends ListCrudRepository<ContractorInvoice, Long>
{
	@Query("SELECT i FROM #{#entityName} i WHERE i.contractor = :contractor and i.startDate <= :now AND i.endDate >= :now")
	Optional<ContractorInvoice> findCurrentContractorInvoice(Contractor contractor, ZonedDateTime now);
}
