package com.greenfieldcommerce.greenerp.services.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;

@ExtendWith(MockitoExtension.class)
public class ContractorInvoiceMessagingServiceTest
{

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ContractorInvoiceMessagingServiceImpl contractorInvoiceMessagingService;

	@Test
	public void shouldSendContractorInvoiceCreatedMessage()
	{
		final ContractorInvoiceRecord record = mock(ContractorInvoiceRecord.class);
		when(record.contractorId()).thenReturn(1L);

		contractorInvoiceMessagingService.sendContractorInvoiceCreatedMessage(record);

		verify(rabbitTemplate).convertAndSend("contractor-invoice-created","contractor-invoice-created." + record.contractorId(), record);

	}

}
