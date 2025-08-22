package com.greenfieldcommerce.greenerp.mappers;

public interface Mapper<SOURCE, TARGET>
{
	TARGET map(final SOURCE source);
}
