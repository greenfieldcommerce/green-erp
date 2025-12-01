package com.greenfieldcommerce.greenerp.rates.records;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BatchContractorRateRecord(@NotNull Long contractorId, @Valid CreateContractorRateRecord rate)
{
}
