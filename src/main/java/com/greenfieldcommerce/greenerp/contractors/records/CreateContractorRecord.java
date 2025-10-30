package com.greenfieldcommerce.greenerp.contractors.records;

import jakarta.validation.constraints.NotBlank;

public record CreateContractorRecord(@NotBlank String email, @NotBlank String name)
{
}
