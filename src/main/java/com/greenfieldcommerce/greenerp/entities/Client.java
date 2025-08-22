package com.greenfieldcommerce.greenerp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Client
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotBlank(message = "Client name cannot be blank")
	@Size(min = 3, max = 255, message = "Client name must have a length between 3 and 255 characters")
	private String name;
}
