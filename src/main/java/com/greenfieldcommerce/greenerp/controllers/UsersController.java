package com.greenfieldcommerce.greenerp.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.records.user.CreateUserRecord;
import com.greenfieldcommerce.greenerp.records.user.UserRecord;
import com.greenfieldcommerce.greenerp.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController
{

	private final UserService userService;

	public UsersController(final UserService userService)
	{
		this.userService = userService;
	}

	@PostMapping
	public UserRecord register(@Valid @RequestBody  CreateUserRecord record)
	{
		return userService.createUser(record.username(), record.password());
	}
 }
