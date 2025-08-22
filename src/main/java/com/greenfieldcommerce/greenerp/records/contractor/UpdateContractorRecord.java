package com.greenfieldcommerce.greenerp.records.contractor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateContractorRecord(@NotNull Long id, @NotBlank String email, @NotBlank String name)
{
}
