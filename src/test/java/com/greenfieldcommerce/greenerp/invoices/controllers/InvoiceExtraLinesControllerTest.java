package com.greenfieldcommerce.greenerp.invoices.controllers;

import static com.greenfieldcommerce.greenerp.helpers.ContractorInvoiceTestValidations.validateContractorInvoice;
import static com.greenfieldcommerce.greenerp.invoices.controllers.ContractorInvoicesControllerTest.describeInvoiceLinks;
import static com.greenfieldcommerce.greenerp.invoices.controllers.ContractorInvoicesControllerTest.describeInvoiceResponse;
import static com.greenfieldcommerce.greenerp.invoices.controllers.ContractorInvoicesControllerTest.invoiceIdParameterDescription;
import static com.greenfieldcommerce.greenerp.invoices.controllers.ContractorInvoicesControllerTest.invoiceLinksMatcher;
import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.controllers.BaseRestControllerTest;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.CreateInvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.invoices.records.InvoiceExtraAmountLineRecord;
import com.greenfieldcommerce.greenerp.invoices.services.ContractorInvoiceService;

@WebMvcTest(controllers = InvoiceExtraLinesController.class)
public class InvoiceExtraLinesControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	private ContractorInvoiceService contractorInvoiceService;

	@BeforeEach
	public void setup()
	{
		when(contractorInvoiceService.addExtraAmountLineToInvoice(eq(INVALID_RESOURCE_ID), any(Long.class), any(CreateInvoiceExtraAmountLineRecord.class))).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.addExtraAmountLineToInvoice(any(Long.class), eq(INVALID_RESOURCE_ID), any(CreateInvoiceExtraAmountLineRecord.class))).thenThrow(entityNotFoundException());

		when(contractorInvoiceService.patchExtraAmountLine(eq(INVALID_RESOURCE_ID), any(Long.class), any(Long.class), any(CreateInvoiceExtraAmountLineRecord.class))).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.patchExtraAmountLine(any(Long.class), eq(INVALID_RESOURCE_ID), any(Long.class), any(CreateInvoiceExtraAmountLineRecord.class))).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.patchExtraAmountLine(any(Long.class), any(Long.class), eq(INVALID_RESOURCE_ID), any(CreateInvoiceExtraAmountLineRecord.class))).thenThrow(entityNotFoundException());
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldCreateInvoiceExtraLine_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final CreateInvoiceExtraAmountLineRecord createRecord = buildValidExtraLineRecord();
		final InvoiceExtraAmountLineRecord extraLine = new InvoiceExtraAmountLineRecord(VALID_RESOURCE_ID, createRecord.amount(), createRecord.description());
		final ContractorInvoiceRecord result = buildInvoiceRecord(extraLine);

		when(contractorInvoiceService.addExtraAmountLineToInvoice(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(createRecord))).thenReturn(result);

		getMvc().perform(postInvoiceExtraLineRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, createRecord).with(user))
			.andExpect(status().isCreated())
			.andExpect(validateContractorInvoice("$", result, getObjectMapper()))
			.andExpectAll(invoiceLinksMatcher())
			.andDo(document("creating-an-invoice-extra-line",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					describeInvoiceLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription(), invoiceIdParameterDescription()),
					describeCreateOrUpdateInvoiceExtraLineBody(),
					describeInvoiceResponse()
				)
			);

		verify(contractorInvoiceService).addExtraAmountLineToInvoice(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(createRecord));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateInvoiceExtraLineRecordOptions")
	void shouldReturnUnprocessableEntityWhenAddingInvoiceExtraLineWithInvalidData(CreateInvoiceExtraAmountLineRecord invalidRecord) throws Exception
	{
		getMvc().perform(postInvoiceExtraLineRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, invalidRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorInvoiceService, never()).addExtraAmountLineToInvoice(any(Long.class), any(Long.class), any(CreateInvoiceExtraAmountLineRecord.class));
	}

	@ParameterizedTest
	@MethodSource("withAdminUserAndOwnerContractor")
	public void shouldUpdateInvoiceExtraLine_forAdminAndOwner(SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user) throws Exception
	{
		final CreateInvoiceExtraAmountLineRecord createRecord = buildValidExtraLineRecord();
		final InvoiceExtraAmountLineRecord extraLine = new InvoiceExtraAmountLineRecord(VALID_RESOURCE_ID, createRecord.amount(), createRecord.description());
		final ContractorInvoiceRecord result = buildInvoiceRecord(extraLine);

		when(contractorInvoiceService.patchExtraAmountLine(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(createRecord))).thenReturn(result);

		getMvc().perform(patchInvoiceExtraLinesRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, createRecord).with(user))
			.andExpect(status().isOk())
			.andExpect(validateContractorInvoice("$", result, getObjectMapper()))
			.andExpectAll(invoiceLinksMatcher())
			.andDo(document("updating-an-invoice-extra-line",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					describeInvoiceLinks(),
					requestHeaders(describeAdminOrContractorHeader()),
					pathParameters(contractorIdParameterDescription(), invoiceIdParameterDescription(), extraLineIdParameterDescription()),
					describeCreateOrUpdateInvoiceExtraLineBody(),
					describeInvoiceResponse()
				)
			);

		verify(contractorInvoiceService).patchExtraAmountLine(eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(VALID_RESOURCE_ID), eq(createRecord));
	}

	@ParameterizedTest
	@MethodSource("invalidCreateInvoiceExtraLineRecordOptions")
	void shouldReturnUnprocessableEntityWhenUpdatingInvoiceExtraLineWithInvalidData(CreateInvoiceExtraAmountLineRecord invalidRecord) throws Exception
	{
		getMvc().perform(patchInvoiceExtraLinesRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, invalidRecord).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isUnprocessableEntity());

		verify(contractorInvoiceService, never()).patchExtraAmountLine(any(Long.class), any(Long.class), any(Long.class), any(CreateInvoiceExtraAmountLineRecord.class));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		final CreateInvoiceExtraAmountLineRecord record = buildValidExtraLineRecord();
		return Stream.of(postInvoiceExtraLineRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, record), patchInvoiceExtraLinesRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, record));
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		final CreateInvoiceExtraAmountLineRecord record = buildValidExtraLineRecord();
		return Stream.of(postInvoiceExtraLineRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()),
			postInvoiceExtraLineRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()),
			patchInvoiceExtraLinesRequest(INVALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()),
			patchInvoiceExtraLinesRequest(VALID_RESOURCE_ID, INVALID_RESOURCE_ID, VALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()),
			patchInvoiceExtraLinesRequest(VALID_RESOURCE_ID, VALID_RESOURCE_ID, INVALID_RESOURCE_ID, record).with(getJwtRequestPostProcessors().admin()));
	}

	private Stream<CreateInvoiceExtraAmountLineRecord> invalidCreateInvoiceExtraLineRecordOptions()
	{
		final Stream<CreateInvoiceExtraAmountLineRecord> invalidAmountOptions = Stream.of(
			new CreateInvoiceExtraAmountLineRecord(null, "Valid"),
			new CreateInvoiceExtraAmountLineRecord(BigDecimal.valueOf(0), "Valid"));
		final Stream<CreateInvoiceExtraAmountLineRecord> invalidDescriptionOptions = INVALID_STRINGS.stream().map(is -> new CreateInvoiceExtraAmountLineRecord(BigDecimal.valueOf(100), is));
		return Stream.of(invalidAmountOptions, invalidDescriptionOptions).flatMap(option -> option);
	}

	private CreateInvoiceExtraAmountLineRecord buildValidExtraLineRecord()
	{
		return new CreateInvoiceExtraAmountLineRecord(BigDecimal.valueOf(100), "An extra line");
	}


	private static ContractorInvoiceRecord buildInvoiceRecord(final InvoiceExtraAmountLineRecord extraLine)
	{
		return new ContractorInvoiceRecord(VALID_RESOURCE_ID, VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), BigDecimal.valueOf(22), Set.of(extraLine), BigDecimal.valueOf(3600), Currency.getInstance("USD"));
	}

	private MockHttpServletRequestBuilder postInvoiceExtraLineRequest(Long contractorId, Long invoiceId, CreateInvoiceExtraAmountLineRecord record) throws JsonProcessingException
	{
		return post("/contractors/{contractorId}/invoices/{invoiceId}/extra-lines", contractorId, invoiceId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private MockHttpServletRequestBuilder patchInvoiceExtraLinesRequest(Long contractorId, Long invoiceId, Long extraLineId, CreateInvoiceExtraAmountLineRecord record) throws JsonProcessingException
	{
		return patch("/contractors/{contractorId}/invoices/{invoiceId}/extra-lines/{extraLineId}", contractorId, invoiceId, extraLineId).contentType(MediaType.APPLICATION_JSON).content(asJson(record));
	}

	private static RequestFieldsSnippet describeCreateOrUpdateInvoiceExtraLineBody()
	{
		return requestFields(
			fieldWithPath("amount").description("The extra amount line amount"),
			fieldWithPath("description").description("A description for the extra amount line"));
	}

	protected static ParameterDescriptor extraLineIdParameterDescription()
	{
		return parameterWithName("extraLineId").description("Invoice extra line id of the line to be updated");
	}
}