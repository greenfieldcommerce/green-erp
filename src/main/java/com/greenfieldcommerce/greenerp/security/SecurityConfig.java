package com.greenfieldcommerce.greenerp.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig
{

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Collection<GrantedAuthority> authorities = new ArrayList<>();

			List<String> scopes = jwt.getClaimAsStringList("scope");
			if (scopes != null) {
				scopes.forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
			}

			Map<String, Object> realmAccess = jwt.getClaim("realm_access");
			if (realmAccess == null || realmAccess.isEmpty()) {
				return authorities;
			}

			List<String> roles = (List<String>) realmAccess.get("roles");
			if (roles != null) {
				roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
			}

			return authorities;
		});

		converter.setPrincipalClaimName("contractorId");

		return converter;
	}

}
