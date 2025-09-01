package com.greenfieldcommerce.greenerp.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Currency;

import com.greenfieldcommerce.greenerp.exceptions.IllegalInvoiceModificationException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UniqueInvoiceForContractorPerMonth", columnNames = { "contractor_id", "startDate", "endDate" }))
public class ContractorInvoice
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "rateId", nullable = false)
	private ContractorRate rate;

	@ManyToOne
	@JoinColumn(name = "contractor_id", nullable = false)
	private Contractor contractor;

	@Column(nullable = false)
	private Currency currency;

	@Column(nullable = false)
	private ZonedDateTime startDate;

	@Column(nullable = false)
	private ZonedDateTime endDate;

	@Column(nullable = false)
	private BigDecimal numberOfWorkedDays;

	private BigDecimal extraAmount;

	@Column(nullable = false)
	private BigDecimal total;

	@Column(nullable = false)
	private InvoiceStatus status;

	protected ContractorInvoice()
	{
	}

	private ContractorInvoice(ContractorRate rate, BigDecimal numberOfWorkedDays, BigDecimal extraAmount)
	{
		this.rate = rate;
		this.contractor = rate.getContractor();
		this.currency = rate.getCurrency();

		this.status = InvoiceStatus.OPEN;

		this.startDate = ZonedDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIDNIGHT);
		this.endDate = startDate.plusMonths(1).minusSeconds(1);

		this.numberOfWorkedDays = numberOfWorkedDays;
		this.extraAmount = extraAmount;

		this.total = calculateTotalInvoiceAmount();
	}

	public static ContractorInvoice create(@NotNull ContractorRate rate, @NotNull BigDecimal numberOfWorkedDays, @NotNull BigDecimal extraAmount)
	{
		return new ContractorInvoice(rate, numberOfWorkedDays, extraAmount);
	}

	private BigDecimal calculateTotalInvoiceAmount()
	{
		return this.numberOfWorkedDays.multiply(this.rate.getRate()).add(this.extraAmount).setScale(2, RoundingMode.HALF_UP);
	}

	public ZonedDateTime getStartDate()
	{
		return startDate;
	}

	public ZonedDateTime getEndDate()
	{
		return endDate;
	}

	public BigDecimal getNumberOfWorkedDays()
	{
		return numberOfWorkedDays;
	}

	public BigDecimal getExtraAmount()
	{
		return extraAmount;
	}

	public BigDecimal getTotal()
	{
		return total;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public void setNumberOfWorkedDays(final BigDecimal numberOfWorkedDays)
	{
		if (!isOpen()) throw new IllegalInvoiceModificationException("NOT_OPEN_INVOICE_MODIFICATION", "Cannot change number of worked days after invoice has been billed");

		this.numberOfWorkedDays = numberOfWorkedDays;
		this.total = calculateTotalInvoiceAmount();
	}

	public void setExtraAmount(final BigDecimal extraAmount)
	{
		if (!isOpen()) throw new IllegalInvoiceModificationException("NOT_OPEN_INVOICE_MODIFICATION", "Cannot change number of worked days after invoice has been billed");

		this.extraAmount = extraAmount;
		this.total = calculateTotalInvoiceAmount();
	}

	public boolean isOpen()
	{
		return InvoiceStatus.OPEN.equals(this.status);
	}

	public void bill()
	{
		this.status = InvoiceStatus.BILLED;
	}

	public void close()
	{
		this.status = InvoiceStatus.CLOSED;
	}

	private enum InvoiceStatus
	{
		OPEN, BILLED, CLOSED
	}
}
