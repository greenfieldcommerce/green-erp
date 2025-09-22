package com.greenfieldcommerce.greenerp.helpers;

import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

public class JwtRequestPostProcessors
{

	@Autowired
	private JwtAuthenticationConverter jwtAuthenticationConverter;

	public SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor admin()
	{
		return jwt().jwt(jwt -> jwt
				.claim("sub", "admin-user")
				.claim("realm_access", Map.of("roles", List.of(AuthenticationConstraint.ROLE_ADMIN))))
			.authorities(jwt -> {
				AbstractAuthenticationToken token = jwtAuthenticationConverter.convert(jwt);
				return token.getAuthorities();
			});
	}

	public SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor regularContractor()
	{
		return jwt().jwt(jwt -> jwt
				.claim("sub", "contractor-user")
				.claim("contractorId", "contractor-user")
				.claim("realm_access", Map.of("roles", List.of(AuthenticationConstraint.ROLE_CONTRACTOR))))
			.authorities(jwt -> {
				AbstractAuthenticationToken token = jwtAuthenticationConverter.convert(jwt);
				return token.getAuthorities();
			});
	}

	public SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor ownContractor()
	{
		return jwt().jwt(jwt -> jwt
				.claim("sub", "contractor-owner")
				.claim("contractorId", String.valueOf(VALID_RESOURCE_ID))
				.claim("realm_access", Map.of("roles", List.of(AuthenticationConstraint.ROLE_CONTRACTOR))))
			.authorities(jwt -> {
				AbstractAuthenticationToken token = jwtAuthenticationConverter.convert(jwt);
				return token.getAuthorities();
			});
	}
}
