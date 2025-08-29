package com.greenfieldcommerce.greenerp.records.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRecord(@NotBlank String username, @NotBlank String password) { }
