package com.greenfieldcommerce.greenerp.entities;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class ContractorRate
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "contractor_id", nullable = false)
	private Contractor contractor;

	@Column(nullable = false)
	private BigDecimal rate;

	@Column(nullable = false)
	private Currency currency;

	@Column(nullable = false)
	private ZonedDateTime startDateTime;

	@Column(nullable = false)
	private ZonedDateTime endDateTime;

	@Version
	private Long version;

	public Long getId()
	{
		return id;
	}

	public void setId(final Long id)
	{
		this.id = id;
	}

	public Contractor getContractor()
	{
		return contractor;
	}

	public void setContractor(final Contractor contractor)
	{
		this.contractor = contractor;
	}

	public BigDecimal getRate()
	{
		return rate;
	}

	public void setRate(final BigDecimal rate)
	{
		this.rate = rate;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(final Currency currency)
	{
		this.currency = currency;
	}

	public ZonedDateTime getStartDateTime()
	{
		return startDateTime;
	}

	public void setStartDateTime(final ZonedDateTime startDateTime)
	{
		this.startDateTime = startDateTime;
	}

	public ZonedDateTime getEndDateTime()
	{
		return endDateTime;
	}

	public void setEndDateTime(final ZonedDateTime endDateTime)
	{
		this.endDateTime = endDateTime;
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
