package com.greenfieldcommerce.greenerp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.greenfieldcommerce.greenerp.helpers.ContractorRateTestValidations.validContractorRate;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.records.ZonedDateTimeRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.services.ContractorRateService;

@WebMvcTest(controllers = ContractorRatesController.class)
public class ContractorRatesControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	private ContractorRateService contractorRateService;

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldReturnContractorRates_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final ContractorRateRecord a = new ContractorRateRecord(1L, BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		final ContractorRateRecord b = new ContractorRateRecord(2L, BigDecimal.valueOf(100.50), Currency.getInstance("USD"), ZonedDateTime.now().plusMonths(1), ZonedDateTime.now().plusMonths(3));

		when(contractorRateService.findRatesForContractor(eq(VALID_RESOURCE_ID))).thenReturn(List.of(a, b));

		getMvc().perform(getContractorRateRequest(VALID_RESOURCE_ID).with(user))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(validContractorRate("$[0]", a, getObjectMapper()))
			.andExpect(validContractorRate("$[1]", b, getObjectMapper()))
			.andDo(
				document("listing-contractor-rates",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription()),
					responseFields(
						subsectionWithPath("[]").description("An array with the contractor's <<resources_rate, Rate resources>>")
					)
				)
			);

		verify(contractorRateService).findRatesForContractor(eq(VALID_RESOURCE_ID));
	}

	@Test
	public void shouldReturnAnEmptyBodyWhenNoCurrentRateExists() throws Exception
	{
		when(contractorRateService.findRatesForContractor(eq(VALID_RESOURCE_ID))).thenReturn(new ArrayList<>());

		getMvc().perform(getContractorRateRequest(VALID_RESOURCE_ID).with(admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));

		verify(contractorRateService).findRatesForContractor(eq(VALID_RESOURCE_ID));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorRateRecordOptions")
	public void shouldReturnUnprocessableEntityWhenCreatingContractorRateWithInvalidData(CreateContractorRateRecord record) throws Exception
	{
		getMvc().perform(postContractorRateRequest(VALID_RESOURCE_ID, record).with(admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorRateService, never()).create(any(Long.class), any(CreateContractorRateRecord.class));
	}

	@Test
	public void shouldCreateContractorRate_forAdmin() throws Exception
	{
		final CreateContractorRateRecord createContractorRateRecord = buildValidContractorRate();
		final ContractorRateRecord result = buildExpectedSuccessResult(createContractorRateRecord);

		when(contractorRateService.create(eq(VALID_RESOURCE_ID), argThat(matchesRate(createContractorRateRecord)))).thenReturn(result);

		getMvc().perform(postContractorRateRequest(VALID_RESOURCE_ID, createContractorRateRecord).with(admin()))
			.andExpect(status().isCreated())
			.andExpect(validContractorRate("$", result, getObjectMapper()))
			.andDo(document("creating-a-rate",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
					pathParameters(contractorIdParameterDescription()),
					requestFields(
						fieldWithPath("rate").description("The contractor's rate"),
						fieldWithPath("currency").description("The rate currency"),
						fieldWithPath("startDateTime").description("Date and time when the rate starts being valid ('valid from')"),
						fieldWithPath("endDateTime").description("Date and time when the rate stops being valid ('valid until')")
					)
				)
			);

		verify(contractorRateService).create(eq(VALID_RESOURCE_ID), argThat(matchesRate(createContractorRateRecord)));
	}

	@Test
	public void shouldReturnUnprocessableEntityWhenUpdatingContractorRateWithInvalidDate() throws Exception
	{
		final ZonedDateTimeRecord record = new ZonedDateTimeRecord(null);
		getMvc().perform(patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, record).with(admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorRateService, never()).changeEndDateTime(any(Long.class), any(Long.class), any(ZonedDateTime.class));
	}

	@Test
	public void shouldUpdateContractorRate_forAdmin() throws Exception
	{
		final ZonedDateTimeRecord zonedDateTimeRecord = buildValidEndDateTimeRecord();
		final ContractorRateRecord result = new ContractorRateRecord(1L, BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), zonedDateTimeRecord.newEndDateTime());

		when(contractorRateService.changeEndDateTime(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), argThat(r -> r.toInstant().equals(zonedDateTimeRecord.newEndDateTime().toInstant())))).thenReturn(result);

		getMvc().perform(patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, zonedDateTimeRecord).with(admin()))
			.andExpect(status().isOk())
			.andExpect(validContractorRate("$", result, getObjectMapper()));

		verify(contractorRateService).changeEndDateTime(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), argThat(r -> r.toInstant().equals(zonedDateTimeRecord.newEndDateTime().toInstant())));
	}

	@Test
	public void shouldDeleteContractorRate_forAdmin() throws Exception
	{
		getMvc().perform(deleteContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID).with(admin())).andExpect(status().isNoContent());
		verify(contractorRateService).delete(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(getContractorRateRequest(VALID_RESOURCE_ID), postContractorRateRequest(VALID_RESOURCE_ID, buildValidContractorRate()), patchContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, buildValidEndDateTimeRecord()),
			deleteContractorRateRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			getContractorRateRequest(INVALID_RESOURCE_ID).with(admin()),
			postContractorRateRequest(INVALID_RESOURCE_ID, buildValidContractorRate()).with(admin()),
			patchContractorRateRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID, buildValidEndDateTimeRecord()).with(admin()),
			patchContractorRateRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID, buildValidEndDateTimeRecord()).with(admin()),
			deleteContractorRateRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID).with(admin()),
			deleteContractorRateRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID).with(admin())
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

	private MockHttpServletRequestBuilder getContractorRateRequest(Long contractorId)
	{
		return get("/contractors/{contractorId}/rates", contractorId);
	}

	private MockHttpServletRequestBuilder postContractorRateRequest(Long contractorId, CreateContractorRateRecord record) throws JsonProcessingException
	{
		return post("/contractors/{contractorId}/rates", contractorId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder patchContractorRateRequest(Long contractorId, Long rateId, ZonedDateTimeRecord record) throws JsonProcessingException
	{
		return patch("/contractors/{contractorId}/rates/{rateId}", contractorId, rateId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder deleteContractorRateRequest(Long contractorId, Long rateId)
	{
		return delete("/contractors/{contractorId}/rates/{rateId}", contractorId, rateId);
	}

	private CreateContractorRateRecord buildValidContractorRate()
	{
		return new CreateContractorRateRecord(BigDecimal.valueOf(100), Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
	}

	private static ContractorRateRecord buildExpectedSuccessResult(final CreateContractorRateRecord createContractorRateRecord)
	{
		return new ContractorRateRecord(1L, createContractorRateRecord.rate(), createContractorRateRecord.currency(), createContractorRateRecord.startDateTime(), createContractorRateRecord.endDateTime());
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