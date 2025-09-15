package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.greenfieldcommerce.greenerp.security.SecurityConfig;

@TestConfiguration
@Import(SecurityConfig.class)
public class TestSecurityConfig
{
}
