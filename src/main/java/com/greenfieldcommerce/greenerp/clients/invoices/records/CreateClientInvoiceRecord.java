package com.greenfieldcommerce.greenerp.clients.invoices.records;

import jakarta.validation.constraints.NotEmpty;

public record CreateClientInvoiceRecord(@NotEmpty String contractorInvoices)
{
}
