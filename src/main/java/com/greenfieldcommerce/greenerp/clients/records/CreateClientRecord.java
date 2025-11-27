package com.greenfieldcommerce.greenerp.clients.records;

import jakarta.validation.constraints.NotBlank;

public record CreateClientRecord(@NotBlank String name, @NotBlank String email)
{
}
