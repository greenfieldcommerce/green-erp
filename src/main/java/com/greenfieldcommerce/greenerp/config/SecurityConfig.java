package com.greenfieldcommerce.greenerp.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig
{
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		var converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Collection<GrantedAuthority> authorities = new ArrayList<>();

			var scopes = jwt.getClaimAsStringList("scope");
			if (scopes != null) {
				scopes.forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
			}

			var roles = jwt.getClaimAsStringList("roles");
			if (roles != null) {
				roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
			}

			return authorities;
		});
		return converter;
	}

}
