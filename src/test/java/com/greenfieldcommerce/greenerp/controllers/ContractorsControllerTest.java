package com.greenfieldcommerce.greenerp.controllers;

import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.emptyContractorRate;
import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.validContractorRate;
import static com.greenfieldcommerce.greenerp.helpers.ContractorTestValidations.validContractor;
import static config.ResolverTestConfig.INVALID_RESOURCE_ID;
import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.services.ContractorService;

@WebMvcTest(controllers = ContractorsController.class)
public class ContractorsControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	public ContractorService contractorService;

	@Test
	void shouldReturnAllContractors_forAdmin() throws Exception
	{
		final ContractorRecord diego = buildFullContractorExample();
		final ContractorRecord jorge = new ContractorRecord(2L, "jorge@greenfieldcommerce.com", "Jorge Viegas", null);
		when(contractorService.findAll()).thenReturn(List.of(diego, jorge));

		getMvc().perform(getContractorsRequest().with(admin())
			).andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(validContractor("$[0]", diego))
			.andExpect(validContractorRate("$[0].currentRate", diego.currentRate(), getObjectMapper()))
			.andExpect(validContractor("$[1]", jorge))
			.andExpect(emptyContractorRate("$[1].currentRate"))
			.andDo(
				document("listing-contractors",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
					responseFields(
						subsectionWithPath("[]").description("An array of <<resources_contractor, Contractor resources>>")
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
			.andDo(
				document("detailing-contractor",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(parameterWithName("contractorId").description("Contractor id")),
					describeContractorResponse()
				)
			);
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorRecordOptions")
	void shouldReturnUnprocessableEntityWhenCreatingContractorWithInvalidData(CreateContractorRecord invalidRecord) throws Exception
	{
		getMvc().perform(createContractorRequest(invalidRecord).with(admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorService, never()).create(any(CreateContractorRecord.class));
	}

	@Test
	void shouldCreateContractor_forAdmin() throws Exception
	{
		final CreateContractorRecord createContractorRecord = buildValidContractor();
		final ContractorRecord result = new ContractorRecord(1L, createContractorRecord.email(), createContractorRecord.name(), null);
		when(contractorService.create(argThat(matchesContractor(createContractorRecord)))).thenReturn(result);

		getMvc().perform(createContractorRequest(createContractorRecord).with(admin()))
			.andExpect(status().isCreated())
			.andExpect(validContractor("$", result))
			.andExpect(emptyContractorRate("$.currentRate"))
			.andDo(document("creating-a-contractor",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
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
			.andDo(document("updating-a-contractor",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(parameterWithName("contractorId").description("Contractor id")),
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
			updateContractorRequest(INVALID_RESOURCE_ID, buildValidContractor()).with(admin()),
			getContractorDetailsRequest(INVALID_RESOURCE_ID).with(admin())
		);
	}

	private static ResponseFieldsSnippet describeContractorResponse()
	{
		return responseFields(
			fieldWithPath("id").description("The unique identifier of the contractor"),
			fieldWithPath("email").description("The contractor's email"),
			fieldWithPath("name").description("The contractor's name"),
			subsectionWithPath("currentRate").description("The contractor's currently active <<resources_rate, rate>>, if any").optional());
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
		final ContractorRateRecord rate = new ContractorRateRecord(1L, BigDecimal.TEN, Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		final ContractorRecord contractor = new ContractorRecord(1L, "diego@greenfieldcommerce.com", "Diego Reidel", rate);

		return contractor;
	}

	private static CreateContractorRecord buildValidContractor()
	{
		return new CreateContractorRecord("gabriel@greenfieldcommerce.com", "Gabriel");
	}

	public interface ContractorRateDocumentation {
		FieldDescriptor[] RATE_FIELDS = new FieldDescriptor[] {
			fieldWithPath("id").description("The customer ID"),
			fieldWithPath("rate").description("The customer's first name"),
			fieldWithPath("currency").description("The customer's first name"),
			fieldWithPath("startDateTime").description("The customer's first name"),
			fieldWithPath("endDateTime").description("The customer's last name")
		};
	}
}
