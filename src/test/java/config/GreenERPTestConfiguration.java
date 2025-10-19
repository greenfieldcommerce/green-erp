package config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.greenfieldcommerce.greenerp.assemblers.ContractorInvoiceModelAssembler;
import com.greenfieldcommerce.greenerp.assemblers.ContractorModelAssembler;
import com.greenfieldcommerce.greenerp.assemblers.ContractorRateModelAssembler;
import com.greenfieldcommerce.greenerp.helpers.JwtRequestPostProcessors;

@TestConfiguration
@EnableAutoConfiguration(exclude = RabbitAutoConfiguration.class)
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

	@Bean
	public RabbitTemplate rabbitTemplate()
	{
		return mock(RabbitTemplate.class);
	}

	@Bean
	public RabbitHealthIndicator rabbitHealthIndicator()
	{
		final RabbitHealthIndicator indicator = mock(RabbitHealthIndicator.class);
		when(indicator.health()).thenReturn(Health.up().build());
		return indicator;
	}
}
