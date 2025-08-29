package com.greenfieldcommerce.greenerp.services;

import com.greenfieldcommerce.greenerp.records.user.UserRecord;

public interface UserService
{
	UserRecord findUserByUsername(String username);
	UserRecord createUser(String username, String password);
}
