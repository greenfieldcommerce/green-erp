package com.greenfieldcommerce.greenerp.mappers.user;

import org.springframework.stereotype.Component;

import com.greenfieldcommerce.greenerp.entities.User;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.user.UserRecord;

@Component
public class UserToRecordMapper implements Mapper<User, UserRecord>
{
	@Override
	public UserRecord map(final User user)
	{
		return new UserRecord(user.getId(), user.getUsername());
	}
}
