package com.greenfieldcommerce.greenerp.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureTestDatabase
@Import(MessagingConfig.class)
public class MessagingConfigTest
{
	private final RabbitTemplate template;

	@Autowired
	public MessagingConfigTest(final RabbitTemplate template)
	{
		this.template = template;
	}

	@Test
	public void shouldUseJacksonMessageConverterWhenSendingMessages()
	{
		assert (this.template != null);
		assert (this.template.getMessageConverter() instanceof Jackson2JsonMessageConverter);
	}
}
