package com.greenfieldcommerce.greenerp.controllers;

import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

import config.ResolverTestConfig;
import config.TestSecurityConfig;

@AutoConfigureMockMvc
@Import({ ResolverTestConfig.class, TestSecurityConfig.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestDocs
abstract class BaseRestControllerTest
{

	protected static final List<String> INVALID_STRINGS = List.of("", " ", "\t", "\n");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtAuthenticationConverter jwtAuthenticationConverter;

	@ParameterizedTest
	@MethodSource("protectedRequests")
	void shouldReturnUnauthorizedWhenRequestingProtectedResources_forUnauthenticated(final RequestBuilder request) throws Exception
	{
		getMvc().perform(request).andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("adminOnlyRequests")
	void shouldReturnForbiddenWhenRequestingProtectedResources_forUnauthorizedUsers(final MockHttpServletRequestBuilder request) throws Exception
	{
		getMvc().perform(request.with(regularContractor()))
			.andExpect(status().isForbidden());
	}

	@ParameterizedTest
	@MethodSource("invalidResourceRequests")
	void shouldReturnNotFoundWhenRequestingWithInvalidResource(final RequestBuilder request) throws Exception
	{
		getMvc().perform(request).andExpect(status().isNotFound());
	}

	protected abstract Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException;

	protected abstract Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException;

	protected Stream<MockHttpServletRequestBuilder> adminOnlyRequests() throws JsonProcessingException
	{
		return Stream.concat(protectedRequests(), Stream.of());
	}

	protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor admin()
	{
		return jwt().jwt(jwt -> jwt
				.claim("sub", "admin-user")
				.claim("realm_access", Map.of("roles", List.of(AuthenticationConstraint.ROLE_ADMIN))))
			.authorities(jwt -> {
				AbstractAuthenticationToken token = jwtAuthenticationConverter.convert(jwt);
				return token.getAuthorities();
			});
	}

	protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor regularContractor()
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

	protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor ownContractor()
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

	protected Stream<SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor> withAdminUserAndOwnerContractor() {
		return Stream.of(admin(), ownContractor());
	}

	protected String asJson(Object object) throws JsonProcessingException
	{
		return objectMapper.writeValueAsString(object);
	}

	protected MockMvc getMvc() {
		return mvc;
	}

	protected ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	protected static HeaderDescriptor describeAdminHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role.").optional();
	}

	protected static HeaderDescriptor describeAdminOrContractorHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role or be owned by the associated 'CONTRACTOR'").optional();
	}

}
