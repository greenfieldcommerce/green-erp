package com.greenfieldcommerce.greenerp.controllers;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.helpers.JwtRequestPostProcessors;

import config.GreenERPTestConfiguration;
import config.TestSecurityConfig;

@AutoConfigureMockMvc
@Import({ TestSecurityConfig.class, GreenERPTestConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestDocs
abstract class BaseRestControllerTest
{

	protected static final List<String> INVALID_STRINGS = List.of("", " ", "\t", "\n");
	protected static final Long VALID_RESOURCE_ID = 1L;
	protected static final Long INVALID_RESOURCE_ID = -1L;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtRequestPostProcessors jwtRequestPostProcessors;

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
		getMvc().perform(request.with(jwtRequestPostProcessors.regularContractor())).andExpect(status().isForbidden());
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

	protected Stream<SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor> withAdminUserAndOwnerContractor()
	{
		return Stream.of(jwtRequestPostProcessors.admin(), jwtRequestPostProcessors.ownContractor());
	}

	protected String asJson(Object object) throws JsonProcessingException
	{
		return objectMapper.writeValueAsString(object);
	}

	protected MockMvc getMvc()
	{
		return mvc;
	}

	protected ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	protected JwtRequestPostProcessors getJwtRequestPostProcessors()
	{
		return jwtRequestPostProcessors;
	}

	protected static HeaderDescriptor describeAdminHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role.").optional();
	}

	protected static HeaderDescriptor describeAdminOrContractorHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role or be owned by the associated 'CONTRACTOR'").optional();
	}

	protected static HeaderDescriptor describeResourceLocationHeader()
	{
		return headerWithName("Location").description("The URL of the newly created resource");
	}

	protected static ParameterDescriptor contractorIdParameterDescription()
	{
		return parameterWithName("contractorId").description("Contractor id");
	}

	protected static ParameterDescriptor contractorRateIdParameterDescription()
	{
		return parameterWithName("rateId").description("Rate id");
	}

	protected EntityNotFoundException entityNotFoundException()
	{
		return new EntityNotFoundException("ERROR", "Entity not found");
	}

}
