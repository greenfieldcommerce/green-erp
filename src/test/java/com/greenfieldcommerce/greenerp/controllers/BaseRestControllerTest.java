package com.greenfieldcommerce.greenerp.controllers;

import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
abstract class BaseRestControllerTest
{

	protected static final List<String> INVALID_STRINGS = List.of("", " ", "\t", "\n");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

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
		getMvc().perform(request.with(regularContractor())).andExpect(status().isForbidden());
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

	protected static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin()
	{
		return SecurityMockMvcRequestPostProcessors.user("admin-user").roles(AuthenticationConstraint.ROLE_ADMIN);
	}

	protected static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor regularContractor()
	{
		return SecurityMockMvcRequestPostProcessors.user("contractor-user").roles(AuthenticationConstraint.ROLE_CONTRACTOR);
	}

	protected static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor ownContractor()
	{
		return SecurityMockMvcRequestPostProcessors.user(String.valueOf(VALID_RESOURCE_ID)).roles(AuthenticationConstraint.ROLE_CONTRACTOR);
	}

	protected static Stream<SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor> withAdminUserAndOwnerContractor() {
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

}
