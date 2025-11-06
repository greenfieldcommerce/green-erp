package com.greenfieldcommerce.greenerp.invoices.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class InvoiceExtraAmountLine
{
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "contractorInvoiceId", nullable = false)
	private ContractorInvoice invoice;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private String description;

	protected InvoiceExtraAmountLine() {}

	private InvoiceExtraAmountLine(ContractorInvoice invoice, BigDecimal amount, String description)
	{
		this.invoice = invoice;
		this.amount = amount;
		this.description = description;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Long getId()
	{
		return id;
	}

	public ContractorInvoice getInvoice()
	{
		return invoice;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(final BigDecimal amount)
	{
		this.amount = amount;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public static InvoiceExtraAmountLine create(ContractorInvoice invoice, BigDecimal amount, String description)
	{
		return new InvoiceExtraAmountLine(invoice, amount, description);
	}
}
