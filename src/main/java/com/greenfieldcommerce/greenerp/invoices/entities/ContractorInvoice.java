package com.greenfieldcommerce.greenerp.invoices.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import com.greenfieldcommerce.greenerp.contractors.entities.Contractor;
import com.greenfieldcommerce.greenerp.rates.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.exceptions.IllegalInvoiceModificationException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UniqueInvoiceForContractorPerMonth", columnNames = { "contractorId", "startDate", "endDate" }))
public class ContractorInvoice
{
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "rateId", nullable = false)
	private ContractorRate rate;

	@ManyToOne
	@JoinColumn(name = "contractorId", nullable = false)
	private Contractor contractor;

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "invoice")
	private Set<InvoiceExtraAmountLine> extraAmountLines;

	@Column(nullable = false)
	private Currency currency;

	@Column(nullable = false)
	private ZonedDateTime startDate;

	@Column(nullable = false)
	private ZonedDateTime endDate;

	@Column(nullable = false)
	private BigDecimal numberOfWorkedDays;

	@Column(nullable = false)
	private BigDecimal total;

	@Column(nullable = false)
	private InvoiceStatus status;

	protected ContractorInvoice()
	{
	}

	private ContractorInvoice(ContractorRate rate, BigDecimal numberOfWorkedDays)
	{
		this.rate = rate;
		this.contractor = rate.getContractor();
		this.currency = rate.getCurrency();
		this.extraAmountLines = new HashSet<>();

		this.status = InvoiceStatus.OPEN;

		this.startDate = ZonedDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIDNIGHT);
		this.endDate = startDate.plusMonths(1).minusSeconds(1);

		this.numberOfWorkedDays = numberOfWorkedDays;

		this.total = calculateTotalInvoiceAmount();
	}

	public static ContractorInvoice create(@NotNull ContractorRate rate, @NotNull BigDecimal numberOfWorkedDays)
	{
		return new ContractorInvoice(rate, numberOfWorkedDays);
	}

	private BigDecimal calculateTotalInvoiceAmount()
	{
		final BigDecimal extraAmount = this.getExtraAmountLines().stream().map(InvoiceExtraAmountLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		return this.numberOfWorkedDays.multiply(this.rate.getRate()).add(extraAmount).setScale(2, RoundingMode.HALF_UP);
	}

	public Contractor getContractor()
	{
		return contractor;
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

	public BigDecimal getTotal()
	{
		return total;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public ContractorRate getRate()
	{
		return rate;
	}

	public void setNumberOfWorkedDays(final BigDecimal numberOfWorkedDays)
	{
		if (!isOpen()) throw new IllegalInvoiceModificationException("NOT_OPEN_INVOICE_MODIFICATION", "Cannot change number of worked days after invoice has been billed");

		this.numberOfWorkedDays = numberOfWorkedDays;
		this.total = calculateTotalInvoiceAmount();
	}

	public void addExtraAmountLine(final InvoiceExtraAmountLine extraAmountLine)
	{
		this.extraAmountLines.add(extraAmountLine);
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

	public Set<InvoiceExtraAmountLine> getExtraAmountLines()
	{
		return extraAmountLines;
	}

	private enum InvoiceStatus
	{
		OPEN, BILLED, CLOSED
	}
}
