package com.greenfieldcommerce.greenerp.invoices.controllers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.greenfieldcommerce.greenerp.helpers.ContractorInvoiceTestValidations.validateContractorInvoice;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.controllers.BaseRestControllerTest;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.services.ContractorInvoiceService;

@WebMvcTest(ContractorInvoicesController.class)
public class ContractorInvoicesControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	private ContractorInvoiceService contractorInvoiceService;

	@BeforeEach
	public void setup()
	{
		when(contractorInvoiceService.findCurrentInvoiceForContractor(INVALID_RESOURCE_ID)).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.create(eq(INVALID_RESOURCE_ID), any(BigDecimal.class), any(BigDecimal.class))).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.patchInvoice(eq(INVALID_RESOURCE_ID), any(BigDecimal.class), any(BigDecimal.class))).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.findByContractor(eq(INVALID_RESOURCE_ID), any(Pageable.class))).thenThrow(entityNotFoundException());
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldReturnLatestInvoices_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final Pageable pageable = buildPageable();

		final ContractorInvoiceRecord invoice1 = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(3600), Currency.getInstance("USD"));
		final ContractorInvoiceRecord invoice2 = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now().minusMonths(1), ZonedDateTime.now().minusMonths(1).plusMonths(1), BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(3600), Currency.getInstance("USD"));
		final ContractorInvoiceRecord invoice3 = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now().minusMonths(2), ZonedDateTime.now().minusMonths(2).plusMonths(1), BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(3600), Currency.getInstance("USD"));

		final List<ContractorInvoiceRecord> invoices = List.of(invoice1, invoice2, invoice3);
		final Page<ContractorInvoiceRecord> page = new PageImpl<>(invoices, pageable, invoices.size());

		when(contractorInvoiceService.findByContractor(eq(VALID_RESOURCE_ID), any(Pageable.class))).thenReturn(page);

		getMvc().perform(getLatestInvoices(VALID_RESOURCE_ID, pageable).with(user))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.invoices").isArray())
			.andExpect(validateContractorInvoice("_embedded.invoices[0]", invoice1, getObjectMapper()))
			.andExpect(validateContractorInvoice("_embedded.invoices[1]", invoice2, getObjectMapper()))
			.andExpect(jsonPath("_links").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.next").exists())
			.andExpect(jsonPath("_links.first").exists())
			.andExpect(jsonPath("_links.last").exists())
			.andExpect(jsonPath("page").exists())
			.andDo(MockMvcResultHandlers.print())
			.andDo(document("listing-latest-invoices",
				preprocessResponse(prettyPrint()),
				requestHeaders(describeAdminOrContractorHeader()),
				requestHeaders(describeAdminOrContractorHeader()),
				queryParameters(
					parameterWithName("page").description("The requested response page, defaults to 0").optional(),
					parameterWithName("size").description("The page size, defaults to 12").optional(),
					parameterWithName("sort").description("The sorting option, defaults to startDate,desc").optional()
				),
				pathParameters(contractorIdParameterDescription()),
				links(
					linkWithRel("self").description("Self link to this page of <<resources_invoices, invoices>>"),
					linkWithRel("next").description("Link to the next page of <<resources_invoices, invoices>>").optional(),
					linkWithRel("first").description("Link to the first page of <<resources_invoices, invoices>>").optional(),
					linkWithRel("last").description("Link to the last page of <<resources_invoices, invoices>>").optional()),
				responseFields(
					subsectionWithPath("_embedded.invoices").description("An array of <<resources_invoice, Invoice resources>>"),
					subsectionWithPath("_links").description("<<resources_invoices_links, Links>> to other resources"),
					subsectionWithPath("page").description("Page metadata")
				)
			));

	}

	@Test
	public void shouldReturnNotFoundWhenContractorHasNoCurrentInvoice() throws Exception
	{
		when(contractorInvoiceService.findCurrentInvoiceForContractor(eq(VALID_RESOURCE_ID))).thenThrow(new EntityNotFoundException("ERROR", "No current invoice"));

		getMvc().perform(getCurrentInvoiceRequest(VALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isNotFound());

		verify(contractorInvoiceService).findCurrentInvoiceForContractor(eq(VALID_RESOURCE_ID));
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldReturnCurrentInvoice_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final ContractorInvoiceRecord record = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(3600), Currency.getInstance("USD"));

		when(contractorInvoiceService.findCurrentInvoiceForContractor(eq(VALID_RESOURCE_ID))).thenReturn(record);

		getMvc().perform(getCurrentInvoiceRequest(VALID_RESOURCE_ID).with(user))
			.andExpect(status().isOk())
			.andExpect(validateContractorInvoice("$", record, getObjectMapper()))
			.andExpectAll(invoiceLinksMatcher())
			.andDo(document("detailing-current-invoice",
				preprocessResponse(prettyPrint()),
				describeInvoiceLinks(),
				requestHeaders(describeAdminOrContractorHeader()),
				pathParameters(contractorIdParameterDescription()),
				responseFields(
					fieldWithPath("contractorId").description("ID of the contractor"),
					fieldWithPath("startDate").description("The start of the period for which the invoice is valid"),
					fieldWithPath("endDate").description("The end of the period for which the invoice is valid"),
					fieldWithPath("numberOfWorkedDays").description("The number of days worked by the contractor"),
					fieldWithPath("total").description("The invoice total"),
					fieldWithPath("extraAmount").description("Any extra amount included in the invoice"),
					fieldWithPath("currency").description("The invoice currency"),
					subsectionWithPath("_links").description("HATEOAS links to related resources"))
			));

		verify(contractorInvoiceService).findCurrentInvoiceForContractor(eq(VALID_RESOURCE_ID));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorInvoiceRecordOptions")
	public void shouldReturnUnprocessableEntityWhenCreatingContractorInvoiceWithInvalidData(CreateContractorInvoiceRecord record) throws Exception
	{
		getMvc().perform(postContractorInvoiceRequest(VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldCreateInvoice_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final CreateContractorInvoiceRecord createContractorInvoiceRecord = buildValidContractorInvoiceRecord();
		final ContractorInvoiceRecord result = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), createContractorInvoiceRecord.numberOfWorkedDays(), createContractorInvoiceRecord.extraAmount(), BigDecimal.valueOf(3600), Currency.getInstance("USD"));

		when(contractorInvoiceService.create(eq(VALID_RESOURCE_ID), eq(createContractorInvoiceRecord.numberOfWorkedDays()), eq(createContractorInvoiceRecord.extraAmount()))).thenReturn(result);

		getMvc().perform(postContractorInvoiceRequest(VALID_RESOURCE_ID, createContractorInvoiceRecord).with(user))
			.andExpect(status().isCreated())
			.andExpect(validateContractorInvoice("$", result, getObjectMapper()))
			.andExpect(header().string("Location", String.format("http://localhost:8080/contractors/%s/invoices/current", result.contractorId())))
			.andExpectAll(invoiceLinksMatcher())
			.andDo(document("creating-an-invoice",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					describeInvoiceLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					responseHeaders(describeResourceLocationHeader()),
					pathParameters(contractorIdParameterDescription()),
					describeCreateOrUpdateContractorInvoiceBody()
				)
			);

		verify(contractorInvoiceService).create(eq(VALID_RESOURCE_ID), eq(createContractorInvoiceRecord.numberOfWorkedDays()), eq(createContractorInvoiceRecord.extraAmount()));
	}

	private static RequestFieldsSnippet describeCreateOrUpdateContractorInvoiceBody()
	{
		return requestFields(fieldWithPath("numberOfWorkedDays").description("The number of days worked by the contractor"), fieldWithPath("extraAmount").description("Any extra amount to be included in the invoice"));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateContractorInvoiceRecordOptions")
	public void shouldReturnUnprocessableEntityWhenUpdatingContractorInvoiceWithInvalidData(CreateContractorInvoiceRecord record) throws Exception
	{
		getMvc().perform(patchCurrentInvoiceRequest(VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldUpdateCurrentInvoice_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final CreateContractorInvoiceRecord createContractorInvoiceRecord = buildValidContractorInvoiceRecord();
		final ContractorInvoiceRecord result = new ContractorInvoiceRecord(VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), createContractorInvoiceRecord.numberOfWorkedDays(), createContractorInvoiceRecord.extraAmount(), BigDecimal.valueOf(3600), Currency.getInstance("USD"));

		when(contractorInvoiceService.patchInvoice(eq(VALID_RESOURCE_ID), eq(createContractorInvoiceRecord.numberOfWorkedDays()), eq(createContractorInvoiceRecord.extraAmount()))).thenReturn(result);

		getMvc().perform(patchCurrentInvoiceRequest(VALID_RESOURCE_ID, createContractorInvoiceRecord).with(user))
			.andExpect(status().isOk())
			.andExpect(validateContractorInvoice("$", result, getObjectMapper()))
			.andExpectAll(invoiceLinksMatcher())
			.andDo(document("updating-an-invoice",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					describeInvoiceLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription()), describeCreateOrUpdateContractorInvoiceBody()
				)
			);

		verify(contractorInvoiceService).patchInvoice(eq(VALID_RESOURCE_ID), eq(createContractorInvoiceRecord.numberOfWorkedDays()), eq(createContractorInvoiceRecord.extraAmount()));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(
			getLatestInvoices(VALID_RESOURCE_ID, buildPageable()),
			getCurrentInvoiceRequest(VALID_RESOURCE_ID),
			postContractorInvoiceRequest(VALID_RESOURCE_ID, buildValidContractorInvoiceRecord()),
			patchCurrentInvoiceRequest(VALID_RESOURCE_ID, buildValidContractorInvoiceRecord())
		);
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			getLatestInvoices(INVALID_RESOURCE_ID, buildPageable()).with(getJwtRequestPostProcessors().admin()),
			getCurrentInvoiceRequest(INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()),
			postContractorInvoiceRequest(INVALID_RESOURCE_ID, buildValidContractorInvoiceRecord()).with(getJwtRequestPostProcessors().admin()),
			patchCurrentInvoiceRequest(INVALID_RESOURCE_ID, buildValidContractorInvoiceRecord()).with(getJwtRequestPostProcessors().admin())
		);
	}

	private Stream<CreateContractorInvoiceRecord> invalidCreateContractorInvoiceRecordOptions()
	{
		return Stream.of(
			new CreateContractorInvoiceRecord(null, BigDecimal.valueOf(100)),
			new CreateContractorInvoiceRecord(BigDecimal.valueOf(-1), BigDecimal.valueOf(100)),
			new CreateContractorInvoiceRecord(BigDecimal.valueOf(32), BigDecimal.valueOf(100)),
			new CreateContractorInvoiceRecord(BigDecimal.valueOf(20), null),
			new CreateContractorInvoiceRecord(BigDecimal.valueOf(20), BigDecimal.valueOf(-1))
		);
	}

	private MockHttpServletRequestBuilder getLatestInvoices(Long contractorId, Pageable pageable)
	{
		final String sort = pageable.getSort().stream()
			.map(order -> order.getProperty() + "," + order.getDirection().name())
			.collect(Collectors.joining("&sort=")); // for multiple orders
		return get("/contractors/{contractorId}/invoices?page={page}&size={size}&sort={sort}", contractorId, pageable.getPageNumber(), pageable.getPageSize(), sort);
	}

	private static Pageable buildPageable()
	{
		final Sort sort = Sort.by(Sort.Direction.DESC, "startDate");
		return PageRequest.of(0, 2, sort);
	}

	private MockHttpServletRequestBuilder getCurrentInvoiceRequest(Long contractorId)
	{
		return get("/contractors/{contractorId}/invoices/current", contractorId);
	}

	private MockHttpServletRequestBuilder postContractorInvoiceRequest(Long contractorId, CreateContractorInvoiceRecord record) throws JsonProcessingException
	{
		return post("/contractors/{contractorId}/invoices", contractorId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder patchCurrentInvoiceRequest(Long contractorId, CreateContractorInvoiceRecord record) throws JsonProcessingException
	{
		return patch("/contractors/{contractorId}/invoices/current", contractorId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private CreateContractorInvoiceRecord buildValidContractorInvoiceRecord()
	{
		return new CreateContractorInvoiceRecord(BigDecimal.valueOf(22), BigDecimal.valueOf(100.50));
	}

	private static ResultMatcher[] invoiceLinksMatcher()
	{
		return new ResultMatcher[] {
			jsonPath("_links").exists(),
			jsonPath("_links.self").exists(),
			jsonPath("_links.self.href").value(String.format("http://localhost:8080/contractors/%s/invoices/current", VALID_RESOURCE_ID)),
			jsonPath("_links.contractor").exists(),
			jsonPath("_links.contractor.href").value(String.format("http://localhost:8080/contractors/%s", VALID_RESOURCE_ID))
		};
	}

	private static LinksSnippet describeInvoiceLinks()
	{
		return links(linkWithRel("self").description("Self link to this <<resources_invoice, Invoice>>"), linkWithRel("contractor").description("Link to the <<resources_contractor, Contractor>> for whom this invoice is issued"));
	}
}
