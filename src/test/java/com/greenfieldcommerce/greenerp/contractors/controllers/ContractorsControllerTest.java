package com.greenfieldcommerce.greenerp.contractors.controllers;

import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.emptyContractorRate;
import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.validContractorRate;
import static com.greenfieldcommerce.greenerp.helpers.ContractorTestValidations.validContractor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.controllers.BaseRestControllerTest;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;

@WebMvcTest(controllers = ContractorsController.class)
public class ContractorsControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	public ContractorService contractorService;

	@BeforeEach
	public void setup()
	{
		when(contractorService.existsById(INVALID_RESOURCE_ID)).thenThrow(entityNotFoundException());
		when(contractorService.findById(INVALID_RESOURCE_ID)).thenThrow(entityNotFoundException());
		when(contractorService.update(eq(INVALID_RESOURCE_ID), any(CreateContractorRecord.class))).thenThrow(entityNotFoundException());
	}

	@Test
	void shouldReturnAllContractors_forAdmin() throws Exception
	{
		final ContractorRecord diego = buildFullContractorExample();
		final ContractorRecord jorge = new ContractorRecord(2L, "jorge@greenfieldcommerce.com", "Jorge Viegas", null);
		when(contractorService.findAll()).thenReturn(List.of(diego, jorge));

		getMvc().perform(getContractorsRequest().with(getJwtRequestPostProcessors().admin())
			).andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.contractors").isArray())
			.andExpect(validContractor("_embedded.contractors[0]", diego))
			.andExpect(validContractorRate("_embedded.contractors[0].currentRate", diego.currentRate(), getObjectMapper()))
			.andExpect(validContractor("_embedded.contractors[1]", jorge))
			.andExpect(emptyContractorRate("_embedded.contractors[1].currentRate"))
			.andExpect(jsonPath("_links").exists())
			.andExpect(jsonPath("$._links.self").exists())
			.andExpect(jsonPath("$._links.self.href").value("http://localhost:8080/contractors"))
			.andDo(
				document("listing-contractors",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
					links(linkWithRel("self").description("Self link to this <<resources_contractors, resource>>")),
					responseFields(
						subsectionWithPath("_embedded.contractors").description("An array of <<resources_contractor, Contractor resources>>"),
						subsectionWithPath("_links").description("<<resources_contractors_links, Links>> to other resources")
					)
				)
			);
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	void shouldReturnContractorDetails_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt) throws Exception
	{
		final ContractorRecord expected = buildFullContractorExample();
		when(contractorService.findById(eq(VALID_RESOURCE_ID))).thenReturn(expected);

		getMvc().perform(getContractorDetailsRequest(VALID_RESOURCE_ID).with(jwt)).andExpect(status().isOk())
			.andExpect(validContractor("$", expected))
			.andExpect(validContractorRate("$.currentRate", expected.currentRate(), getObjectMapper()))
			.andExpectAll(contractorLinksMatchers(VALID_RESOURCE_ID))
			.andDo(print())
			.andDo(
				document("detailing-contractor",
					preprocessResponse(prettyPrint()),
					describeContractorLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription()),
					describeContractorResponse()
				)
			);
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorRecordOptions")
	void shouldReturnUnprocessableEntityWhenCreatingContractorWithInvalidData(CreateContractorRecord invalidRecord) throws Exception
	{
		getMvc().perform(createContractorRequest(invalidRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorService, never()).create(any(CreateContractorRecord.class));
	}

	@Test
	void shouldCreateContractor_forAdmin() throws Exception
	{
		final CreateContractorRecord createContractorRecord = buildValidContractor();
		final ContractorRecord result = new ContractorRecord(3L, createContractorRecord.email(), createContractorRecord.name(), null);
		when(contractorService.create(argThat(matchesContractor(createContractorRecord)))).thenReturn(result);

		getMvc().perform(createContractorRequest(createContractorRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isCreated())
			.andExpect(validContractor("$", result))
			.andExpect(emptyContractorRate("$.currentRate"))
			.andExpect(header().string("Location", String.format("http://localhost:8080/contractors/%s", result.id())))
			.andExpectAll(contractorLinksMatchers(result.id()))
			.andDo(document("creating-a-contractor",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(describeAdminHeader()),
				responseHeaders(describeResourceLocationHeader()),
				describeContractorLinks(),
				requestFields(
					fieldWithPath("email").description("The contractor's email"),
					fieldWithPath("name").description("The contractor's name"))
				)
			);

		verify(contractorService).create(any(CreateContractorRecord.class));
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	void shouldReturnUpdatedContractorWhenUpdatingWithValidData_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt) throws Exception
	{
		final CreateContractorRecord createContractorRecord = buildValidContractor();
		final ContractorRecord result = new ContractorRecord(VALID_RESOURCE_ID, createContractorRecord.email(), createContractorRecord.name(), null);
		when(contractorService.update(eq(VALID_RESOURCE_ID), argThat(matchesContractor(createContractorRecord)))).thenReturn(result);

		getMvc().perform(updateContractorRequest(VALID_RESOURCE_ID, createContractorRecord).with(jwt))
			.andExpect(status().isOk()).andExpect(validContractor("$", result))
			.andExpect(emptyContractorRate("$.currentRate"))
			.andExpectAll(contractorLinksMatchers(result.id()))
			.andDo(document("updating-a-contractor",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				describeContractorLinks(),
				requestHeaders(describeAdminOrContractorHeader()),
				pathParameters(contractorIdParameterDescription()),
				requestFields(
					fieldWithPath("email").description("The updated contractor's email"),
					fieldWithPath("name").description("The updated contractor's name"))
				)
			);

		verify(contractorService).update(eq(VALID_RESOURCE_ID), argThat(matchesContractor(createContractorRecord)));
	}

	//@formatter:off
	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(
			getContractorsRequest(),
			getContractorDetailsRequest(VALID_RESOURCE_ID),
			createContractorRequest(buildValidContractor()),
			updateContractorRequest(VALID_RESOURCE_ID, buildValidContractor())
		);
	}
	//@formatter:on

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			updateContractorRequest(INVALID_RESOURCE_ID, buildValidContractor()).with(getJwtRequestPostProcessors().admin()),
			getContractorDetailsRequest(INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin())
		);
	}

	private static ResponseFieldsSnippet describeContractorResponse()
	{
		return responseFields(
			fieldWithPath("id").description("The unique identifier of the contractor"),
			fieldWithPath("email").description("The contractor's email"),
			fieldWithPath("name").description("The contractor's name"),
			subsectionWithPath("currentRate").description("The contractor's currently active <<resources_rate, rate>>, if any").optional(),
			subsectionWithPath("_links").description("HATEOAS links to related resources"));
	}

	private Stream<CreateContractorRecord> invalidCreateContractorRecordOptions()
	{
		final Stream<CreateContractorRecord> invalidEmailOptions = INVALID_STRINGS.stream().map(invalidEmail -> new CreateContractorRecord(invalidEmail, "name"));
		final Stream<CreateContractorRecord> invalidNameOptions = INVALID_STRINGS.stream().map(invalidName -> new CreateContractorRecord("email", invalidName));
		final Stream<CreateContractorRecord> nullOptions = Stream.of(
			new CreateContractorRecord(null, "name"),
			new CreateContractorRecord("email", null));

		return Stream.of(invalidEmailOptions, invalidNameOptions, nullOptions).flatMap(option -> option);
	}

	private ArgumentMatcher<CreateContractorRecord> matchesContractor(final CreateContractorRecord createContractorRecord)
	{
		return record -> {
			boolean nameMatches = record.name().equals(createContractorRecord.name());
			boolean emailMatches = record.email().equals(createContractorRecord.email());
			return nameMatches && emailMatches;
		};
	}

	private static MockHttpServletRequestBuilder getContractorsRequest()
	{
		return get("/contractors");
	}

	private static MockHttpServletRequestBuilder getContractorDetailsRequest(Long contractorId)
	{
		return get("/contractors/{contractorId}", contractorId);
	}

	private MockHttpServletRequestBuilder createContractorRequest(CreateContractorRecord record) throws JsonProcessingException
	{
		return post("/contractors").contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder updateContractorRequest(Long contractorId, CreateContractorRecord record) throws JsonProcessingException
	{
		return patch("/contractors/{contractorId}", contractorId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private static ContractorRecord buildFullContractorExample()
	{
		final ContractorRateRecord rate = new ContractorRateRecord(1L, 1L, BigDecimal.TEN, Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		return new ContractorRecord(1L, "diego@greenfieldcommerce.com", "Diego Reidel", rate);
	}

	private static CreateContractorRecord buildValidContractor()
	{
		return new CreateContractorRecord("gabriel@greenfieldcommerce.com", "Gabriel");
	}

	private static ResultMatcher[] contractorLinksMatchers(Long contractorId) {
		return new ResultMatcher[] {
			jsonPath("$._links").exists(),
			jsonPath("$._links.self").exists(),
			jsonPath("$._links.self.href").value("http://localhost:8080/contractors/" + contractorId),
			jsonPath("$._links.rates").exists(),
			jsonPath("$._links.rates.href").value(String.format("http://localhost:8080/contractors/%s/rates", contractorId)),
			jsonPath("$._links.currentInvoice").exists(),
			jsonPath("$._links.currentInvoice.href").value(String.format("http://localhost:8080/contractors/%s/invoices/current", contractorId))
		};
	}

	private LinksSnippet describeContractorLinks()
	{
		return links(
			linkWithRel("self").description("Self link to this <<resources_contractor, Contractor>>"),
			linkWithRel("rates").description("Link to this contractor's <<resources_rates, Rates>>"),
			linkWithRel("currentInvoice").description("Link to this contractor's <<resources_invoice, current Invoice>>"),
			linkWithRel("latestInvoices").description("Link to this contractor's latest <<resources_invoices, Invoices>>")
		);
	}
}
