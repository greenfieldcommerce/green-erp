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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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

/**
 * Abstract base class for REST controller integration tests.
 * <p>
 * This class provides common testing functionality for REST controllers including:
 * <ul>
 * <li>Security testing (authentication and authorization)</li>
 * <li>Spring REST Docs configuration and utilities</li>
 * <li>MockMvc setup and configuration</li>
 * <li>Common test data and helper methods</li>
 * <li>Parameterized tests for protected endpoints</li>
 * </ul>
 * <p>
 * Subclasses should extend this class and implement the abstract methods to provide
 * controller-specific test cases.
 */
@AutoConfigureMockMvc
@Import({ TestSecurityConfig.class, GreenERPTestConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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

	/**
	 * Tests that protected resources return 401 Unauthorized when accessed without authentication.
	 * <p>
	 * This parameterized test verifies that all protected endpoints properly enforce authentication
	 * by rejecting requests that lack valid JWT tokens.
	 *
	 * @param request the request builder for the endpoint to test
	 * @throws Exception if the request execution fails
	 */
	@ParameterizedTest
	@MethodSource("protectedRequests")
	void shouldReturnUnauthorizedWhenRequestingProtectedResources_forUnauthenticated(final RequestBuilder request) throws Exception
	{
		getMvc().perform(request).andExpect(status().isUnauthorized());
	}

	/**
	 * Tests that admin-only resources return 403 Forbidden when accessed by non-admin users.
	 * <p>
	 * This parameterized test verifies that endpoints requiring admin privileges properly enforce
	 * authorization by rejecting requests from authenticated users without admin role.
	 *
	 * @param request the request builder for the admin-only endpoint to test
	 * @throws Exception if the request execution fails
	 */
	@ParameterizedTest
	@MethodSource("adminOnlyRequests")
	void shouldReturnForbiddenWhenRequestingProtectedResources_forUnauthorizedUsers(final MockHttpServletRequestBuilder request) throws Exception
	{
		getMvc().perform(request.with(jwtRequestPostProcessors.regularContractor())).andExpect(status().isForbidden());
	}

	/**
	 * Tests that requests with invalid resource IDs return 404 Not Found.
	 * <p>
	 * This parameterized test verifies that endpoints properly handle non-existent resources
	 * by returning appropriate 404 status codes.
	 *
	 * @param request the request builder for the endpoint to test with invalid resource
	 * @throws Exception if the request execution fails
	 */
	@ParameterizedTest
	@MethodSource("invalidResourceRequests")
	void shouldReturnNotFoundWhenRequestingWithInvalidResource(final RequestBuilder request) throws Exception
	{
		getMvc().perform(request).andExpect(status().isNotFound());
	}

	/**
	 * Provides a stream of protected request builders for authentication testing.
	 * <p>
	 * Subclasses must implement this method to return all protected endpoints that should
	 * be tested for proper authentication enforcement.
	 *
	 * @return a stream of request builders for protected endpoints
	 * @throws JsonProcessingException if JSON serialization fails during request building
	 */
	protected abstract Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException;

	/**
	 * Provides a stream of request builders with invalid resource IDs for error handling testing.
	 * <p>
	 * Subclasses must implement this method to return endpoints that should be tested with
	 * invalid resource identifiers.
	 *
	 * @return a stream of request builders with invalid resource references
	 * @throws JsonProcessingException if JSON serialization fails during request building
	 */
	protected abstract Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException;

	/**
	 * Provides a stream of admin-only request builders for authorization testing.
	 * <p>
	 * The default implementation returns all protected requests. Subclasses can override this
	 * to specify which endpoints require admin privileges specifically.
	 *
	 * @return a stream of request builders for admin-only endpoints
	 * @throws JsonProcessingException if JSON serialization fails during request building
	 */
	protected Stream<MockHttpServletRequestBuilder> adminOnlyRequests() throws JsonProcessingException
	{
		return Stream.concat(protectedRequests(), Stream.of());
	}

	/**
	 * Provides a stream of JWT request post-processors for users with admin role or the resource owner.
	 * <p>
	 * Returns post-processors for both admin users and contractor owners, useful for testing
	 * endpoints that allow access by either role.
	 *
	 * @return a stream of JWT request post-processors for admin and owner contractor roles
	 */
	protected Stream<SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor> withAdminUserAndOwnerContractor()
	{
		return Stream.of(jwtRequestPostProcessors.admin(), jwtRequestPostProcessors.ownContractor());
	}

	/**
	 * Converts an object to its JSON string representation.
	 *
	 * @param object the object to serialize
	 * @return the JSON string representation of the object
	 * @throws JsonProcessingException if serialization fails
	 */
	protected String asJson(Object object) throws JsonProcessingException
	{
		return objectMapper.writeValueAsString(object);
	}

	/**
	 * Creates a REST Docs header descriptor for admin-only endpoints.
	 *
	 * @return a header descriptor for Authorization header requiring admin role
	 */
	protected static HeaderDescriptor describeAdminHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role.").optional();
	}

	/**
	 * Creates a REST Docs header descriptor for endpoints accessible by admin or resource owner.
	 *
	 * @return a header descriptor for Authorization header requiring admin role or contractor ownership
	 */
	protected static HeaderDescriptor describeAdminOrContractorHeader()
	{
		return headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'ADMIN' role or be owned by the associated 'CONTRACTOR'").optional();
	}

	/**
	 * Creates a REST Docs header descriptor for the Location header in creation responses.
	 *
	 * @return a header descriptor for Location header containing the new resource URL
	 */
	protected static HeaderDescriptor describeResourceLocationHeader()
	{
		return headerWithName("Location").description("The URL of the newly created resource");
	}

	/**
	 * Creates a REST Docs parameter descriptor for contractor ID path parameters.
	 *
	 * @return a parameter descriptor for contractorId
	 */
	protected static ParameterDescriptor contractorIdParameterDescription()
	{
		return parameterWithName("contractorId").description("Contractor id");
	}

	/**
	 * Creates a REST Docs parameter descriptor for contractor rate ID path parameters.
	 *
	 * @return a parameter descriptor for rateId
	 */
	protected static ParameterDescriptor contractorRateIdParameterDescription()
	{
		return parameterWithName("rateId").description("Rate id");
	}

	/**
	 * Creates a generic EntityNotFoundException for testing error handling.
	 *
	 * @return a new EntityNotFoundException instance
	 */
	protected EntityNotFoundException entityNotFoundException()
	{
		return new EntityNotFoundException("ERROR", "Entity not found");
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

}
