package com.greenfieldcommerce.greenerp.clients.records;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "clients")
public record ClientRecord(Long id, String name, String email)
{
}
