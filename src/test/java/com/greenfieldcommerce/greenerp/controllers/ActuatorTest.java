package com.greenfieldcommerce.greenerp.controllers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class ActuatorTest
{
	@Autowired
	private MockMvc mvc;

	@Test
	void actuatorEndpointsShouldPermitAll() throws Exception
	{
		mvc.perform(get("/actuator/health")).andExpect(status().isOk())
			.andDo(document("health"));
		mvc.perform(get("/actuator/metrics")).andExpect(status().isOk())
			.andDo(document("metrics"));
	}
}
