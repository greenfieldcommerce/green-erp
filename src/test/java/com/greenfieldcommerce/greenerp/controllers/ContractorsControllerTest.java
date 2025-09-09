package com.greenfieldcommerce.greenerp.controllers;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.helpers.TestDateFormatter;
import com.greenfieldcommerce.greenerp.records.contractor.ContractorRecord;
import com.greenfieldcommerce.greenerp.records.contractorrate.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.services.ContractorService;

import config.ResolverTestConfig;
import config.SecurityConfig;

@WebMvcTest(controllers = ContractorsController.class)
@AutoConfigureMockMvc
@Import({ ResolverTestConfig.class, SecurityConfig.class })
public class ContractorsControllerTest
{

	@MockitoBean
	public ContractorService contractorService;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnUnauthorizedWhenRequestingContractorsWithNoUser() throws Exception
	{
		mvc.perform(get("/contractors")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "contractor-user", roles = { "CONTRACTOR" })
	void shouldReturnForbiddenWhenRequestingContractorsWithNonAdminUser() throws Exception
	{
		mvc.perform(get("/contractors")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin-user", roles = { "ADMIN" })
	void shouldReturnAllContractors() throws Exception
	{
		final ContractorRateRecord rate = new ContractorRateRecord(1L, BigDecimal.TEN, Currency.getInstance("USD"), ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1));
		final ContractorRecord diego = new ContractorRecord(1L, "diego@oneemail.com", "Diego", rate);
		final ContractorRecord jorge = new ContractorRecord(2L, "jorge@twoemail.com", "Jorge", null);

		when(contractorService.findAll()).thenReturn(List.of(diego, jorge));

		mvc.perform(get("/contractors")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(diego.id())).andExpect(jsonPath("$[0].name").value(diego.name()))
			.andExpect(jsonPath("$[0].currentRate.id").value(rate.id())).andExpect(jsonPath("$[0].currentRate.rate").value(rate.rate())).andExpect(jsonPath("$[0].currentRate.currency").value(rate.currency().toString()))
			.andExpect(jsonPath("$[0].currentRate.startDateTime").value(TestDateFormatter.formatDate(objectMapper, rate.startDateTime())))
			.andExpect(jsonPath("$[0].currentRate.endDateTime").value(TestDateFormatter.formatDate(objectMapper, rate.endDateTime()))).andExpect(jsonPath("$[1].id").value(jorge.id())).andExpect(jsonPath("$[1].name").value(jorge.name()))
			.andExpect(jsonPath("$[1].currentRate").doesNotExist());
	}
}
