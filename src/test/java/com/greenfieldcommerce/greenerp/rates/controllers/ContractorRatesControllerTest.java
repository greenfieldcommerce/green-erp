package com.greenfieldcommerce.greenerp.rates.controllers;

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

import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.validContractorRate;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.controllers.BaseRestControllerTest;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.records.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.services.ContractorRateService;

@WebMvcTest(controllers = ContractorRatesController.class)
public class ContractorRatesControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	private ContractorRateService contractorRateService;

	@BeforeEach
	public void setup()
	{
		when(contractorRateService.findEntityById(eq(INVALID_RESOURCE_ID))).thenThrow(entityNotFoundException());
		when(contractorRateService.findRatesForContractor(INVALID_RESOURCE_ID)).thenThrow(entityNotFoundException());
		when(contractorRateService.findByIdAndContractorId(eq(INVALID_RESOURCE_ID), any(Long.class))).thenThrow(entityNotFoundException());
		when(contractorRateService.findByIdAndContractorId(any(Long.class), eq(INVALID_RESOURCE_ID))).thenThrow(entityNotFoundException());
		when(contractorRateService.create(eq(INVALID_RESOURCE_ID), any(CreateContractorRateRecord.class))).thenThrow(entityNotFoundException());
		when(contractorRateService.changeEndDateTime(eq(INVALID_RESOURCE_ID), any(Long.class), any(ZonedDateTime.class))).thenThrow(entityNotFoundException());
		when(contractorRateService.changeEndDateTime(any(Long.class), eq(INVALID_RESOURCE_ID), any(ZonedDateTime.class))).thenThrow(entityNotFoundException());
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldReturnAllContractorRates_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final ContractorRateRecord a = new ContractorRateRecord(VALID_RESOURCE_ID, 1L, BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		final ContractorRateRecord b = new ContractorRateRecord(VALID_RESOURCE_ID, 2L, BigDecimal.valueOf(100.50), Currency.getInstance("USD"), ZonedDateTime.now().plusMonths(1), ZonedDateTime.now().plusMonths(3));

		when(contractorRateService.findRatesForContractor(eq(VALID_RESOURCE_ID))).thenReturn(List.of(a, b));

		getMvc().perform(getAllContractorRatesRequest(VALID_RESOURCE_ID).with(user))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(validContractorRate("_embedded.rates[0]", a, getObjectMapper()))
			.andExpect(validContractorRate("_embedded.rates[1]", b, getObjectMapper()))
			.andExpect(jsonPath("_links").exists())
			.andExpect(jsonPath("$._links.self").exists())
			.andExpect(jsonPath("$._links.self.href").value(String.format("http://localhost:8080/contractors/%s/rates", VALID_RESOURCE_ID)))
			.andDo(
				document("listing-contractor-rates",
					preprocessResponse(prettyPrint()),
					links(linkWithRel("self").description("Self link to this <<resources_rates, resource>>")),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription()),
					responseFields(
						subsectionWithPath("_embedded.rates").description("An array with the contractor's <<resources_rate, Rate resources>>"),
						subsectionWithPath("_links").description("<<resources_rates_links, Links>> to other resources")
					)
				)
			);

		verify(contractorRateService).findRatesForContractor(eq(VALID_RESOURCE_ID));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorRateRecordOptions")
	public void shouldReturnUnprocessableEntityWhenCreatingContractorRateWithInvalidData(CreateContractorRateRecord record) throws Exception
	{
		getMvc().perform(postContractorRateRequest(VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorRateService, never()).create(any(Long.class), any(CreateContractorRateRecord.class));
	}

	@Test
	public void shouldCreateContractorRate_forAdmin() throws Exception
	{
		final CreateContractorRateRecord createContractorRateRecord = buildValidContractorRate();
		final ContractorRateRecord result = buildExpectedSuccessResult(createContractorRateRecord);

		when(contractorRateService.create(eq(VALID_RESOURCE_ID), argThat(matchesRate(createContractorRateRecord)))).thenReturn(result);

		getMvc().perform(postContractorRateRequest(VALID_RESOURCE_ID, createContractorRateRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isCreated())
			.andExpect(validContractorRate("$", result, getObjectMapper()))
			.andExpect(header().string("Location", String.format("http://localhost:8080/contractors/%s/rates/%s", result.contractorId(), result.id())))
			.andExpectAll(rateLinksMatchers())
			.andDo(document("creating-a-rate",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				describeRateLinks(),
				requestHeaders(describeAdminHeader()),
				responseHeaders(describeResourceLocationHeader()),
				pathParameters(contractorIdParameterDescription()),
				describeContractorRateResponse(),
				requestFields(
					fieldWithPath("rate").description("The contractor's daily rate"),
					fieldWithPath("currency").description("The rate currency"),
					fieldWithPath("startDateTime").description("Date and time when the rate starts being valid ('valid from')"),
					fieldWithPath("endDateTime").description("Date and time when the rate stops being valid ('valid until')")
				)));

		verify(contractorRateService).create(eq(VALID_RESOURCE_ID), argThat(matchesRate(createContractorRateRecord)));
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldReturnContractorRateById_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt) throws Exception
	{
		final ContractorRateRecord rate = new ContractorRateRecord(VALID_RESOURCE_ID,1L, BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));

		when(contractorRateService.findByIdAndContractorId(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID))).thenReturn(rate);

		getMvc().perform(getContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID).with(jwt))
			.andExpect(status().isOk())
			.andExpect(validContractorRate("$", rate, getObjectMapper()))
			.andExpectAll(rateLinksMatchers())
			.andDo(
				document("detailing-a-rate",
					preprocessResponse(prettyPrint()),
					describeRateLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription(), contractorRateIdParameterDescription()),
					describeContractorRateResponse()
				)
			);

		verify(contractorRateService).findByIdAndContractorId(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID)  );
	}

	@Test
	public void shouldReturnUnprocessableEntityWhenUpdatingContractorRateWithInvalidDate() throws Exception
	{
		final ZonedDateTimeRecord record = new ZonedDateTimeRecord(null);
		getMvc().perform(patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorRateService, never()).changeEndDateTime(any(Long.class), any(Long.class), any(ZonedDateTime.class));
	}

	@Test
	public void shouldUpdateContractorRate_forAdmin() throws Exception
	{
		final ZonedDateTimeRecord zonedDateTimeRecord = buildValidEndDateTimeRecord();
		final ContractorRateRecord result = new ContractorRateRecord(VALID_RESOURCE_ID,1L, BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), zonedDateTimeRecord.newEndDateTime());

		when(contractorRateService.changeEndDateTime(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), argThat(r -> r.toInstant().equals(zonedDateTimeRecord.newEndDateTime().toInstant())))).thenReturn(result);

		getMvc().perform(patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, zonedDateTimeRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isOk())
			.andExpect(validContractorRate("$", result, getObjectMapper()))
			.andExpectAll(rateLinksMatchers())
			.andDo(
				document("updating-a-rate",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					describeRateLinks(),
					requestHeaders(describeAdminHeader()),
					pathParameters(contractorIdParameterDescription(), contractorRateIdParameterDescription()),
					describeContractorRateResponse(),
					requestFields(
						fieldWithPath("newEndDateTime").description("The updated rate's end date and time")
					)
				)
			);

		verify(contractorRateService).changeEndDateTime(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), argThat(r -> r.toInstant().equals(zonedDateTimeRecord.newEndDateTime().toInstant())));
	}

	@Test
	public void shouldDeleteContractorRate_forAdmin() throws Exception
	{
		getMvc().perform(deleteContractorRateRequest()
			.with(getJwtRequestPostProcessors().admin())).andExpect(status().isNoContent())
			.andDo(document("deleting-a-rate",
				requestHeaders(describeAdminHeader()),
				pathParameters(contractorIdParameterDescription(), contractorRateIdParameterDescription())
			)
		);
		verify(contractorRateService).delete(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(
			getAllContractorRatesRequest(VALID_RESOURCE_ID),
			postContractorRateRequest(VALID_RESOURCE_ID, buildValidContractorRate()),
			getContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID),
			patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, buildValidEndDateTimeRecord()),
			deleteContractorRateRequest());
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			getAllContractorRatesRequest(INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()),
			postContractorRateRequest(INVALID_RESOURCE_ID, buildValidContractorRate()).with(getJwtRequestPostProcessors().admin()),
			getContractorRateRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()),
			getContractorRateRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()),
			patchContractorRateRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID, buildValidEndDateTimeRecord()).with(getJwtRequestPostProcessors().admin()),
			patchContractorRateRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID, buildValidEndDateTimeRecord()).with(getJwtRequestPostProcessors().admin())
		);
	}

	private Stream<CreateContractorRateRecord> invalidCreateContractorRateRecordOptions()
	{
		return Stream.of(
			new CreateContractorRateRecord(null, Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1)),
			new CreateContractorRateRecord(BigDecimal.valueOf(100), null, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1)),
			new CreateContractorRateRecord(BigDecimal.valueOf(100), Currency.getInstance("USD"), null, ZonedDateTime.now().plusMonths(1)),
			new CreateContractorRateRecord(BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), null),
			new CreateContractorRateRecord(BigDecimal.valueOf(-100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1)));
	}

	private MockHttpServletRequestBuilder getAllContractorRatesRequest(Long contractorId)
	{
		return get("/contractors/{contractorId}/rates", contractorId);
	}

	private MockHttpServletRequestBuilder postContractorRateRequest(Long contractorId, CreateContractorRateRecord record) throws JsonProcessingException
	{
		return post("/contractors/{contractorId}/rates", contractorId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder getContractorRateRequest(Long contractorId, Long rateId)
	{
		return get("/contractors/{contractorId}/rates/{rateId}", contractorId, rateId);
	}

	private MockHttpServletRequestBuilder patchContractorRateRequest(Long contractorId, Long rateId, ZonedDateTimeRecord record) throws JsonProcessingException
	{
		return patch("/contractors/{contractorId}/rates/{rateId}", contractorId, rateId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder deleteContractorRateRequest()
	{
		return delete("/contractors/{contractorId}/rates/{rateId}", VALID_RESOURCE_ID, VALID_RESOURCE_ID);
	}

	private CreateContractorRateRecord buildValidContractorRate()
	{
		return new CreateContractorRateRecord(BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
	}

	private static ContractorRateRecord buildExpectedSuccessResult(final CreateContractorRateRecord createContractorRateRecord)
	{
		return new ContractorRateRecord(VALID_RESOURCE_ID, VALID_RESOURCE_ID, createContractorRateRecord.rate(), createContractorRateRecord.currency(), createContractorRateRecord.startDateTime(), createContractorRateRecord.endDateTime());
	}

	private ZonedDateTimeRecord buildValidEndDateTimeRecord()
	{
		return new ZonedDateTimeRecord(ZonedDateTime.now().plusMonths(1));
	}

	private ArgumentMatcher<CreateContractorRateRecord> matchesRate(final CreateContractorRateRecord createContractorRateRecord)
	{
		return record -> {
			boolean rateMatches = record.rate().equals(createContractorRateRecord.rate());
			boolean currencyMatches = record.currency().equals(createContractorRateRecord.currency());
			boolean startDateTimeMatches = record.startDateTime().toInstant().equals(createContractorRateRecord.startDateTime().toInstant());
			boolean endDateTimeMatches = record.endDateTime().toInstant().equals(createContractorRateRecord.endDateTime().toInstant());

			return rateMatches && currencyMatches && startDateTimeMatches && endDateTimeMatches;
		};
	}

	private static ResponseFieldsSnippet describeContractorRateResponse()
	{
		return responseFields(
			fieldWithPath("contractorId").description("The ID of the contractor this rate belongs to"),
			fieldWithPath("id").description("The rate ID"),
			fieldWithPath("rate").description("The contractor's daily rate"),
			fieldWithPath("currency").description("The currency of the rate"),
			fieldWithPath("startDateTime").description("Date and time when the rate starts being valid ('valid from')"),
			fieldWithPath("endDateTime").description("Date and time when the rate stops being valid ('valid until')"),
			subsectionWithPath("_links").description("<<resources_rate_links, Links>> to other resources")
		);
	}

	private static ResultMatcher[] rateLinksMatchers()
	{
		return new ResultMatcher[] {
			jsonPath("$._links").exists(),
			jsonPath("$._links.self").exists(),
			jsonPath("$._links.self.href").value(String.format("http://localhost:8080/contractors/%s/rates/%s", VALID_RESOURCE_ID, VALID_RESOURCE_ID)),
			jsonPath("$._links.contractor").exists(),
			jsonPath("$._links.contractor.href").value(String.format("http://localhost:8080/contractors/%s", VALID_RESOURCE_ID))
		};
	}

	private static LinksSnippet describeRateLinks()
	{
		return links(
			linkWithRel("self").description("Self link to this <<resources_rate, Rate>>"),
			linkWithRel("contractor").description("Lint to the <<resources_contractor, contractor>> this rate belongs to"));
	}
}