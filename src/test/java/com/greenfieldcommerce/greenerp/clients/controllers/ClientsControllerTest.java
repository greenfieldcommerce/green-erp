package com.greenfieldcommerce.greenerp.clients.controllers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;
import com.greenfieldcommerce.greenerp.contractors.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.contractors.invoices.services.ContractorInvoiceService;
import com.greenfieldcommerce.greenerp.controllers.BaseRestControllerTest;

import static com.greenfieldcommerce.greenerp.helpers.ClientTestValidations.validClient;
import static com.greenfieldcommerce.greenerp.helpers.ContractorInvoiceTestValidations.validateContractorInvoice;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientsController.class)
public class ClientsControllerTest extends BaseRestControllerTest
{

	@MockitoBean
	public ClientService clientService;

	@MockitoBean
	public ContractorInvoiceService contractorInvoiceService;

	@BeforeEach
	public void setup()
	{
		when(clientService.findById(INVALID_RESOURCE_ID)).thenThrow(entityNotFoundException());
		when(contractorInvoiceService.findOpenForClientBeforeDate(eq(INVALID_RESOURCE_ID), any(ZonedDateTime.class))).thenThrow(entityNotFoundException());
	}

	@Test
	void shouldReturnAllClients_forAdmin() throws Exception
	{
		final ClientRecord client1 = buildClient();
		final ClientRecord client2 = new ClientRecord(2L, "Second Client", "another@email.com");

		when(clientService.findAll()).thenReturn(List.of(client1, client2));

		getMvc().perform(getClientsRequest().with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.clients").isArray())
			.andExpect(validClient("_embedded.clients[0]", client1))
			.andExpect(validClient("_embedded.clients[1]", client2))
			.andExpect(jsonPath("_links").exists())
			.andExpect(jsonPath("$._links.self").exists())
			.andExpect(jsonPath("$._links.self.href").value("http://localhost:8080/clients"))
			.andDo(
				document("listing-clients",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
					links(linkWithRel("self").description("Self link to this <<resources_clients, resource>>")),
					responseFields(
						subsectionWithPath("_embedded.clients").description("An array of <<resources_client, Client resources>>"),
						subsectionWithPath("_links").description("<<resources_clients_links, Links>> to other resources")
					)
				)
			);
	}

	@Test
	void shouldReturnClientDetails_forAdmin() throws Exception
	{
		final ClientRecord client = buildClient();

		when(clientService.findById(eq(VALID_RESOURCE_ID))).thenReturn(client);

		getMvc().perform(getClientDetailsRequest(VALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isOk())
			.andExpect(validClient("$", client))
			.andExpectAll(clientLinksMatchers(VALID_RESOURCE_ID))
			.andDo(print())
			.andDo(
				document("detailing-client",
					preprocessResponse(prettyPrint()),
					describeClientLinks(),
					requestHeaders(describeAdminHeader()),
					pathParameters(clientIdParameterDescription()),
					describeClientResponse()
				)
			);
	}

	@Test
	void shouldReturnContractorInvoicesForClient_forAdmin() throws Exception
	{
		final ContractorInvoiceRecord invoice1 = new ContractorInvoiceRecord(VALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), BigDecimal.valueOf(20), SetUtils.emptySet(), BigDecimal.valueOf(3600), Currency.getInstance("USD"), "OPEN");
		final ContractorInvoiceRecord invoice2 = new ContractorInvoiceRecord(VALID_RESOURCE_ID, VALID_RESOURCE_ID, VALID_RESOURCE_ID, ZonedDateTime.now().minusMonths(1), ZonedDateTime.now().minusMonths(1).plusMonths(1), BigDecimal.valueOf(20), SetUtils.emptySet(), BigDecimal.valueOf(3600), Currency.getInstance("USD"), "BILLED");

		final List<ContractorInvoiceRecord> invoices = List.of(invoice1, invoice2);

		when(contractorInvoiceService.findOpenForClientBeforeDate(eq(VALID_RESOURCE_ID), any(ZonedDateTime.class))).thenReturn(invoices);

		getMvc().perform(getContractorInvoicesForClientRequest(VALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.invoices").isArray())
			.andExpect(validateContractorInvoice("_embedded.invoices[0]", invoice1, getObjectMapper()))
			.andExpect(validateContractorInvoice("_embedded.invoices[1]", invoice2, getObjectMapper()))
			.andExpect(jsonPath("_links").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andDo(print())
			.andDo(
				document("listing-contractor-invoices-for-client",
					preprocessResponse(prettyPrint()),
					requestHeaders(describeAdminHeader()),
					queryParameters(
						parameterWithName("startDateBefore").description("A date limiter for the invoices (formatted ISO 8601 date-time), defaults to current date").optional()
					),
					pathParameters(clientIdParameterDescription()),
					links(linkWithRel("self").description("Self link to this <<resources_client_contractor_invoices, open invoices query>>")),
					responseFields(
						subsectionWithPath("_embedded.invoices").description("An array of <<resources_invoice, Invoice resources>>"),
						subsectionWithPath("_links").description("<<resources_client_contractor_invoices, Links>> to other resources")
					)
				)
			);
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> protectedRequests() throws JsonProcessingException
	{
		return Stream.of(
			getClientsRequest(),
			getClientDetailsRequest(VALID_RESOURCE_ID),
			getContractorInvoicesForClientRequest(VALID_RESOURCE_ID)
		);
	}

	@Override
	protected Stream<MockHttpServletRequestBuilder> invalidResourceRequests() throws JsonProcessingException
	{
		return Stream.of(
			getClientDetailsRequest(INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin()),
			getContractorInvoicesForClientRequest(INVALID_RESOURCE_ID).with(getJwtRequestPostProcessors().admin())
		);
	}

	private static MockHttpServletRequestBuilder getClientsRequest()
	{
		return get("/clients");
	}

	private static MockHttpServletRequestBuilder getClientDetailsRequest(final Long clientId)
	{
		return get("/clients/{clientId}", clientId);
	}

	private static MockHttpServletRequestBuilder getContractorInvoicesForClientRequest(final Long clientId)
	{
		return get("/clients/{clientId}/contractor-invoices", clientId);
	}

	private static ResultMatcher[] clientLinksMatchers(final Long clientId)
	{
		return new ResultMatcher[] {
			jsonPath("$._links").exists(),
			jsonPath("$._links.self.href").value("http://localhost:8080/clients/" + clientId),
			jsonPath("$._links.allClients.href").value("http://localhost:8080/clients")
		};
	}

	private static ClientRecord buildClient()
	{
		return new ClientRecord(1L, "Client Name", "client@email.com");
	}

	private LinksSnippet describeClientLinks()
	{
		return links(
			linkWithRel("self").description("Self link to this <<resources_client, Client>>"),
			linkWithRel("allClients").description("Link to all <<resources_clients, Clients>>"),
			linkWithRel("openContractorInvoicesForClient").description("Link to the open <<resources_invoices, invoices>> for this client")
		);
	}

	private static ResponseFieldsSnippet describeClientResponse()
	{
		return responseFields(
			fieldWithPath("id").description("The unique identifier of the client"),
			fieldWithPath("email").description("The client's email"),
			fieldWithPath("name").description("The client's name"),
			subsectionWithPath("_links").description("HATEOAS <<resources_client_links, client links>> to related resources")
		);
	}
}
