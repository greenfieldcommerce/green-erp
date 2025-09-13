package com.greenfieldcommerce.greenerp.resolvers;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.ContractorRate;
import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;

@Component
public class ContractorRateIdArgumentResolver extends BaseArgumentResolver<ContractorRate, Long>
{

	public ContractorRateIdArgumentResolver(final ContractorRateRepository repository)
	{
		super(Long.class, repository);
	}

	@Override
	protected String getIdParameterName()
	{
		return "rateId";
	}

	@Override
	protected String getResourceName()
	{
		return "rates";
	}

	@Override
	protected String getDescription()
	{
		return "Rate";
	}

	@Override
	protected Function<String, Long> getIdParser()
	{
		return Long::parseLong;
	}
}
