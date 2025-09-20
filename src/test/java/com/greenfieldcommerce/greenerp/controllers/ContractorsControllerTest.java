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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
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
import org.springframework.restdocs.snippet.Snippet;
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
		final ContractorRateRecord rate = new ContractorRateRecord(1L, BigDecimal.TEN, Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		final ContractorRecord diego = new ContractorRecord(1L, "diego@greenfieldcommerce.com", "Diego Reidel", rate);
		final ContractorRecord jorge = new ContractorRecord(2L, "diego@greenfieldcommerce.com", "Jorge Viegas", null);
		when(contractorService.findAll()).thenReturn(List.of(diego, jorge));

		getMvc().perform(get("/contractors")
				.with(admin())
			).andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(validContractor("$[0]", diego))
			.andExpect(validContractorRate("$[0].currentRate", rate, getObjectMapper()))
			.andExpect(validContractor("$[1]", jorge))
			.andExpect(emptyContractorRate("$[1].currentRate"))
			.andDo(
				document("listing-contractors",
					requestHeaders(
						headerWithName("Authorization").description("Bearer token used to authenticate the request. Must have 'admin' role.").optional()
					),
					responseFields(
						fieldWithPath("[]").description("An array of items"), // Optional: documents the array itself
						fieldWithPath("[].id").description("The unique identifier of the item"),
						fieldWithPath("[].email").description("The name of the item"),
						fieldWithPath("[].name").description("The name of the item"),
						subsectionWithPath("[].currentRate").description("A description of the item").optional())
//					.("[].currentRate.", ContractorRateDocumentation.RATE_FIELDS)
				)
			);
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

	@ParameterizedTest
	@MethodSource("invalidCreateContractorRecordOptions")
	void shouldReturnUnprocessableEntityWhenCreatingContractorWithInvalidData(CreateContractorRecord invalidRecord) throws Exception
	{
		getMvc().perform(post("/contractors").with(admin())
			.contentType(MediaType.APPLICATION_JSON).content(asJson(invalidRecord)))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorService, never()).create(any(CreateContractorRecord.class));
	}

	@Test
	void shouldCreateContractor_forAdmin() throws Exception
	{
		final CreateContractorRecord createContractorRecord = buildValidContractor();
		final String body = asJson(createContractorRecord);
		final ContractorRecord result = new ContractorRecord(1L, createContractorRecord.email(), createContractorRecord.name(), null);
		when(contractorService.create(argThat(matchesContractor(createContractorRecord)))).thenReturn(result);

		getMvc().perform(post("/contractors").with(admin()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(validContractor("$", result)).andExpect(emptyContractorRate("$[0].currentRate"));

		verify(contractorService).create(any(CreateContractorRecord.class));
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	void shouldReturnUpdatedContractorWhenUpdatingWithValidData_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt) throws Exception
	{
		final CreateContractorRecord createContractorRecord = buildValidContractor();
		final ContractorRecord result = new ContractorRecord(VALID_RESOURCE_ID, createContractorRecord.email(), createContractorRecord.name(), null);
		when(contractorService.update(eq(VALID_RESOURCE_ID), argThat(matchesContractor(createContractorRecord)))).thenReturn(result);

		getMvc().perform(patch("/contractors/{id}", VALID_RESOURCE_ID).with(jwt)
				.contentType(MediaType.APPLICATION_JSON).content(asJson(createContractorRecord))).andExpect(status().isOk()).andExpect(validContractor("$", result))
			.andExpect(emptyContractorRate("$[0].currentRate"));

		verify(contractorService).update(eq(VALID_RESOURCE_ID), argThat(matchesContractor(createContractorRecord)));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(
			get("/contractors"),
			post("/contractors").contentType(MediaType.APPLICATION_JSON).content(asJson(buildValidContractor())),
			patch("/contractors/{id}", VALID_RESOURCE_ID).contentType(MediaType.APPLICATION_JSON).content(asJson(buildValidContractor())));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			patch("/contractors/{id}", INVALID_RESOURCE_ID).with(admin()).contentType(MediaType.APPLICATION_JSON).content(asJson(buildValidContractor()))
		);
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

	private static CreateContractorRecord buildValidContractor()
	{
		return new CreateContractorRecord("email", "name");
	}

}
