package com.greenfieldcommerce.greenerp.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.greenfieldcommerce.greenerp.resolvers.ContractorIdArgumentResolver;
import com.greenfieldcommerce.greenerp.resolvers.ContractorRateIdArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer
{
	private final ContractorIdArgumentResolver contractorIdResolver;
	private final ContractorRateIdArgumentResolver rateIdResolver;

	public WebConfig(ContractorIdArgumentResolver contractorResolver, final ContractorRateIdArgumentResolver rateIdResolver)
	{
		this.contractorIdResolver = contractorResolver;
		this.rateIdResolver = rateIdResolver;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
	{
		resolvers.add(contractorIdResolver);
		resolvers.add(rateIdResolver);
	}
}
