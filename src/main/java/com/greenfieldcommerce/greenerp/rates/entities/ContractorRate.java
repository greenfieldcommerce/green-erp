package com.greenfieldcommerce.greenerp.rates.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Currency;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
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

	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@Column(nullable = false)
	private BigDecimal rate;

	@Column(nullable = false)
	private BigDecimal externalRate;

	@Column(nullable = false)
	private BigDecimal taxDeduction;

	@Column(nullable = false)
	private BigDecimal margin;

	@Column(nullable = false)
	private BigDecimal grossRate;

	@Column(nullable = false)
	private Currency currency;

	@Column(nullable = false)
	private ZonedDateTime startDateTime;

	@Column(nullable = false)
	private ZonedDateTime endDateTime;

	@Version
	private Long version;

	protected ContractorRate(){}

	private ContractorRate(final Contractor contractor, final Client client,
		final BigDecimal rate, final BigDecimal externalRate, final BigDecimal taxDeduction, final BigDecimal margin, final BigDecimal grossRate,
		final Currency currency, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime)
	{
		this.contractor = contractor;
		this.client = client;
		this.rate = rate;
		this.externalRate = externalRate;
		this.taxDeduction = taxDeduction;
		this.margin = margin;
		this.grossRate = grossRate;
		this.currency = currency;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	public static ContractorRate create(final Contractor contractor, final Client client,
		final BigDecimal rate, final BigDecimal externalRate, final BigDecimal taxDeduction,
		final Currency currency, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime)
	{
		final BigDecimal divisor = BigDecimal.ONE.divide(taxDeduction.divide(BigDecimal.valueOf(100.0), RoundingMode.HALF_UP), RoundingMode.HALF_UP);
		final BigDecimal grossRate = rate.divide(divisor, RoundingMode.HALF_UP);
		final BigDecimal margin = BigDecimal.valueOf(100.0).multiply(BigDecimal.ONE.subtract(grossRate.divide(externalRate, RoundingMode.HALF_UP)));

		return new ContractorRate(contractor, client, rate, externalRate, taxDeduction, margin, grossRate, currency, startDateTime, endDateTime);
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

	public Client getClient()
	{
		return client;
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
