package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.greenfieldcommerce.greenerp.helpers.JwtRequestPostProcessors;

@TestConfiguration
public class GreenERPTestConfiguration
{
	@Bean
	public JwtRequestPostProcessors jwtRequestPostProcessors()
	{
		return new JwtRequestPostProcessors();
	}
}
