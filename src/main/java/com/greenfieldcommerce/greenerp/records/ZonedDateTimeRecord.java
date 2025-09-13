package com.greenfieldcommerce.greenerp.records;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

public record ZonedDateTimeRecord(@NotNull ZonedDateTime newEndDateTime) { }
