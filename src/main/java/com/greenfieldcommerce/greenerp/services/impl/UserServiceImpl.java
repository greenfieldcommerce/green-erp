package com.greenfieldcommerce.greenerp.services.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.entities.User;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.mappers.Mapper;
import com.greenfieldcommerce.greenerp.records.user.UserRecord;
import com.greenfieldcommerce.greenerp.repositories.UserRepository;
import com.greenfieldcommerce.greenerp.services.UserService;

@Service
public class UserServiceImpl implements UserService
{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final Mapper<User, UserRecord> userToRecordMapper;

	public UserServiceImpl(final UserRepository userRepository, final PasswordEncoder passwordEncoder, final Mapper<User, UserRecord> userToRecordMapper)
	{
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userToRecordMapper = userToRecordMapper;
	}

	@Override
	public UserRecord findUserByUsername(final String username)
	{
		User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND", "Could not find user " + username));
		return userToRecordMapper.map(user);
	}

	@Override
	public UserRecord createUser(final String username, final String password)
	{
		final User saved = userRepository.save(new User(username, passwordEncoder.encode(password)));
		return userToRecordMapper.map(saved);
	}
}
