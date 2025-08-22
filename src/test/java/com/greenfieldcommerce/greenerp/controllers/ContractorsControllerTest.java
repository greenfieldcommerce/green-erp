package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ContractorsController.class)
public class ContractorsControllerTest
{

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

}