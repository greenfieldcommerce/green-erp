package com.greenfieldcommerce.greenerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class GreenErpApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(GreenErpApplication.class, args);
	}
}
