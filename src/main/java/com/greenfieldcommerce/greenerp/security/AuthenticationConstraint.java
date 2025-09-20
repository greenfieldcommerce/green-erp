package com.greenfieldcommerce.greenerp.security;

public class AuthenticationConstraint
{
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_CONTRACTOR = "ROLE_CONTRACTOR";

	public static final String ALLOW_ADMIN_OR_OWN_CONTRACTOR = "hasRole('ADMIN') or (hasRole('CONTRACTOR') and #contractorId.toString().equals(authentication.token.claims['contractorId']))";
	public static final String ALLOW_ADMIN_ONLY = "hasRole('ADMIN')";
}
