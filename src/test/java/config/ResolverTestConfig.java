package config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.greenfieldcommerce.greenerp.resolvers.ContractorIdArgumentResolver;
import com.greenfieldcommerce.greenerp.resolvers.ContractorRateIdArgumentResolver;

@TestConfiguration
public class ResolverTestConfig
{

	@Bean
	ContractorIdArgumentResolver contractorIdArgumentResolver()
	{
		return Mockito.mock(ContractorIdArgumentResolver.class);
	}

	@Bean
	ContractorRateIdArgumentResolver contractorRateIdArgumentResolver()
	{
		return Mockito.mock(ContractorRateIdArgumentResolver.class);
	}
}