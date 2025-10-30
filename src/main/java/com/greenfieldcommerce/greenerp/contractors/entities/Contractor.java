package com.greenfieldcommerce.greenerp.contractors.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;

import com.greenfieldcommerce.greenerp.invoices.entities.ContractorInvoice;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;

@Entity
public class Contractor
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String name;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "contractor")
	private final List<ContractorRate> rates = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "contractor")
	private final List<ContractorInvoice> invoices = new ArrayList<>();

	@Version
	private Long version;

	protected Contractor() {}

	private Contractor(final String email, final String name)
	{
		this.email = email;
		this.name = name;
	}

	public static Contractor create(String email, String name)
	{
		return new Contractor(email, name);
	}

	public Optional<ContractorRate> getCurrentRate()
	{
		return CollectionUtils.emptyIfNull(rates).stream().filter(ContractorRate::isActive).findFirst();
	}

	public Long getId()
	{
		return id;
	}

	public void setId(final Long id)
	{
		this.id = id;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public List<ContractorRate> getRates()
	{
		return rates;
	}

	public List<ContractorInvoice> getInvoices()
	{
		return invoices;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion(final Long version)
	{
		this.version = version;
	}
}
