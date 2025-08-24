package com.greenfieldcommerce.greenerp.resolvers;

import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import com.greenfieldcommerce.greenerp.entities.Contractor;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;

@Component
public class ContractorIdArgumentResolver extends BaseArgumentResolver<Contractor, Long> implements HandlerMethodArgumentResolver
{

	public ContractorIdArgumentResolver(ContractorRepository contractorRepository)
	{
		super(Long.class, contractorRepository);
	}

	@Override
	protected String getIdParameterName()
	{
		return "contractorId";
	}

	@Override
	protected String getResourceName()
	{
		return "contractors";
	}

	@Override
	protected String getDescription()
	{
		return "Contractor";
	}

	@Override
	protected Function<String, Long> getIdParser()
	{
		return Long::parseLong;
	}
}
