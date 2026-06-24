package com.greenfieldcommerce.greenerp.clients.records;

import java.util.Currency;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClientRecord(@NotBlank String name, @NotBlank String email, @NotNull Currency currency, @NotNull @Min(value = 0) @Max(value = 28) Integer dueDateGap)
{
}
