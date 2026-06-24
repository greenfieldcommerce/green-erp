package com.greenfieldcommerce.greenerp.clients.invoices.entities;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import com.greenfieldcommerce.greenerp.clients.entities.Client;
import com.greenfieldcommerce.greenerp.contractors.invoices.entities.ContractorInvoice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class ClientInvoice
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "clientId", nullable = false)
	private Client client;

	@Column(nullable = false)
	private Currency currency;

	@Column(nullable = false)
	private ZonedDateTime invoiceDate;

	@Column(nullable = false)
	private ZonedDateTime dueDate;

	@Column(nullable = false)
	private BigDecimal total;

	@Column(nullable = false)
	private ClientInvoiceStatus status;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "clientInvoice")
	private final List<ContractorInvoice> contractorInvoices = new ArrayList<>();

	protected ClientInvoice()
	{
	}

	private ClientInvoice(Client client, Currency currency, ZonedDateTime invoiceDate, ZonedDateTime dueDate)
	{
		this.client = client;
		this.currency = currency;
		this.invoiceDate = invoiceDate;
		this.dueDate = dueDate;

		this.total = BigDecimal.ZERO;
		this.status = ClientInvoiceStatus.OPEN;
	}

	public static ClientInvoice create(Client client, List<ContractorInvoice> contractorInvoices)
	{
		final ZonedDateTime invoiceDate = ZonedDateTime.now();
		final ZonedDateTime dueDate = invoiceDate.plusDays(client.getInvoiceDueDateGap());
		final ClientInvoice clientInvoice = new ClientInvoice(client, client.getInvoiceCurrency(), invoiceDate, dueDate);
		contractorInvoices.forEach(clientInvoice::addContractorInvoice);

		return clientInvoice;
	}

	public void bill()
	{
		this.status = ClientInvoiceStatus.BILLED;
	}

	private void addContractorInvoice(ContractorInvoice contractorInvoice)
	{
		contractorInvoices.add(contractorInvoice);
		total = total.add(contractorInvoice.getTotal());
	}

	public Long getId()
	{
		return id;
	}

	public Client getClient()
	{
		return client;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public ZonedDateTime getInvoiceDate()
	{
		return invoiceDate;
	}

	public ZonedDateTime getDueDate()
	{
		return dueDate;
	}

	public BigDecimal getTotal()
	{
		return total;
	}

	public ClientInvoiceStatus getStatus()
	{
		return status;
	}

	public List<ContractorInvoice> getContractorInvoices()
	{
		return contractorInvoices;
	}

	public enum ClientInvoiceStatus
	{
		OPEN, GENERATED, BILLED, CLOSED
	}
}
