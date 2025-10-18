package com.greenfieldcommerce.greenerp.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.greenfieldcommerce.greenerp.records.contractorinvoice.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.services.ContractorInvoiceMessagingService;

@Service
public class ContractorInvoiceMessagingServiceImpl implements ContractorInvoiceMessagingService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractorInvoiceMessagingServiceImpl.class);

	private final RabbitTemplate rabbitTemplate;

	public ContractorInvoiceMessagingServiceImpl(final RabbitTemplate rabbitTemplate)
	{
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendContractorInvoiceCreatedMessage(final ContractorInvoiceRecord contractorInvoiceRecord)
	{
		LOGGER.info("Sending contractor invoice created message for contractor {}", contractorInvoiceRecord.contractorId());
		rabbitTemplate.convertAndSend("contractor-invoice-created", "contractor-invoice-created." + contractorInvoiceRecord.contractorId(), contractorInvoiceRecord);
	}
}
