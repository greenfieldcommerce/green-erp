package com.greenfieldcommerce.greenerp.rates.entities;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;

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

	protected ContractorRate(){}

	private ContractorRate(final Contractor contractor, final BigDecimal rate, final Currency currency, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime)
	{
		this.contractor = contractor;
		this.rate = rate;
		this.currency = currency;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	public static ContractorRate create(final Contractor contractor, final BigDecimal rate, final Currency currency, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime)
	{
		return new ContractorRate(contractor, rate, currency, startDateTime, endDateTime);
	}

	public boolean isActive()
	{
		final ZonedDateTime now = ZonedDateTime.now();
		return startDateTime.isBefore(now) && endDateTime.isAfter(now);
	}

	public void inactivate()
	{
		this.endDateTime = ZonedDateTime.now();
	}

	public void setEndDateTime(final ZonedDateTime endDateTime)
	{
		this.endDateTime = endDateTime;
	}

	public Long getId()
	{
		return id;
	}

	public Contractor getContractor()
	{
		return contractor;
	}

	public BigDecimal getRate()
	{
		return rate;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public ZonedDateTime getStartDateTime()
	{
		return startDateTime;
	}

	public ZonedDateTime getEndDateTime()
	{
		return endDateTime;
	}

}
