package com.greenfieldcommerce.greenerp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static config.ResolverTestConfig.INVALID_RESOURCE_ID;
import static config.ResolverTestConfig.VALID_RESOURCE_ID;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfieldcommerce.greenerp.helpers.JwtRequestPostProcessors;
import com.greenfieldcommerce.greenerp.records.contractor.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.services.ContractorService;

import config.GreenERPTestConfiguration;
import config.ResolverTestConfig;
import config.TestSecurityConfig;

@WebMvcTest(controllers = ContractorsController.class)
@AutoConfigureMockMvc
@Import({ ResolverTestConfig.class, TestSecurityConfig.class, GreenERPTestConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestDocs
public class ErrorResponseExampleTest
{

	@MockitoBean
	public ContractorService contractorService;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JwtRequestPostProcessors jwtRequestPostProcessors;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void errorExample() throws Exception
	{
		final String path = "/contractors/" + VALID_RESOURCE_ID;
		this.mvc.perform(patch(path).with(jwtRequestPostProcessors.admin())
			.contentType("application/json").content(objectMapper.writeValueAsString(new CreateContractorRecord("", ""))))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("error").value("Unprocessable Entity"))
			.andExpect(jsonPath("timestamp").exists())
			.andExpect(jsonPath("status").value("422"))
			.andExpect(jsonPath("path").value(path))
			.andExpect(jsonPath("code").exists())
			.andExpect(jsonPath("message").exists())
			.andExpect(jsonPath("details").exists())
			.andDo(print())
			.andDo(document("error-example",
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`"),
					fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred"),
					fieldWithPath("status").description("The HTTP status code, e.g. `400`"),
					fieldWithPath("path").description("The path to which the request was made"),
					fieldWithPath("code").description("The application-specific error code, e.g an exception code"),
					fieldWithPath("message").description("The error message, e.g. `Invalid ID provided`"),
					subsectionWithPath("details")
						.description("Details on the error, e.g validation errors. Each entry represents a `key -> value` pair, where the key is the invalid field and the value is the reason / description").optional())
			));
	}

}
