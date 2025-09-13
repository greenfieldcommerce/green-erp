package config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.greenfieldcommerce.greenerp.repositories.ContractorRateRepository;
import com.greenfieldcommerce.greenerp.repositories.ContractorRepository;
import com.greenfieldcommerce.greenerp.resolvers.ContractorIdArgumentResolver;
import com.greenfieldcommerce.greenerp.resolvers.ContractorRateIdArgumentResolver;

@TestConfiguration
public class ResolverTestConfig
{

	public static final Long VALID_RESOURCE_ID = 1L;
	public static final Long INVALID_RESOURCE_ID = 9999999999L;

	@Bean
	ContractorIdArgumentResolver contractorIdArgumentResolver()
	{
		final ContractorRepository contractorRepository = Mockito.mock(ContractorRepository.class);
		when(contractorRepository.existsById(eq(VALID_RESOURCE_ID))).thenReturn(true);
		when(contractorRepository.existsById(eq(INVALID_RESOURCE_ID))).thenReturn(false);

		return new ContractorIdArgumentResolver(contractorRepository);
	}

	@Bean
	ContractorRateIdArgumentResolver contractorRateIdArgumentResolver()
	{
		final ContractorRateRepository contractorRateRepository = Mockito.mock(ContractorRateRepository.class);
		when(contractorRateRepository.existsById(eq(VALID_RESOURCE_ID))).thenReturn(true);
		when(contractorRateRepository.existsById(eq(INVALID_RESOURCE_ID))).thenReturn(false);

		return new ContractorRateIdArgumentResolver(contractorRateRepository);
	}
}