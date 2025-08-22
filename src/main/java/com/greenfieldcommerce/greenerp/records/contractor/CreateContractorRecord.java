package com.greenfieldcommerce.greenerp.records.contractor;

import jakarta.validation.constraints.NotBlank;

public record CreateContractorRecord(@NotBlank String email, @NotBlank String name)
{
}
