package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.greenfieldcommerce.greenerp.assemblers.ContractorInvoiceModelAssembler;
import com.greenfieldcommerce.greenerp.assemblers.ContractorModelAssembler;
import com.greenfieldcommerce.greenerp.assemblers.ContractorRateModelAssembler;
import com.greenfieldcommerce.greenerp.helpers.JwtRequestPostProcessors;

@TestConfiguration
public class GreenERPTestConfiguration
{
	@Bean
	public JwtRequestPostProcessors jwtRequestPostProcessors()
	{
		return new JwtRequestPostProcessors();
	}

	@Bean
	public ContractorModelAssembler contractorModelAssembler()
	{
		return new ContractorModelAssembler();
	}

	@Bean
	public ContractorInvoiceModelAssembler contractorInvoiceModelAssembler()
	{
		return new ContractorInvoiceModelAssembler();
	}

	@Bean
	public ContractorRateModelAssembler contractorRateModelAssembler()
	{
		return new ContractorRateModelAssembler();
	}
}
