package com.greenfieldcommerce.greenerp.clients.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Client
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Version
	private Long version;

	protected Client() {}

	private Client(final String name, final String email)
	{
		this.name = name;
		this.email = email;
	}

	public static Client create(final String name, final String email)
	{
		return new Client(name, email);
	}

	public Long getId()
	{
		return id;
	}

	public void setId(final Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
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
