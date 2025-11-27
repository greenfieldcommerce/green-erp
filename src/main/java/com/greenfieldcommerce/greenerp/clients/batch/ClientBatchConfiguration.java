package com.greenfieldcommerce.greenerp.clients.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.greenfieldcommerce.greenerp.batch.AbstractBatchConfiguration;
import com.greenfieldcommerce.greenerp.clients.records.ClientRecord;
import com.greenfieldcommerce.greenerp.clients.records.CreateClientRecord;
import com.greenfieldcommerce.greenerp.clients.services.ClientService;

@Configuration
public class ClientBatchConfiguration extends AbstractBatchConfiguration<CreateClientRecord, ClientRecord>
{
	private final ClientService clientService;

	public ClientBatchConfiguration(final ClientService clientService)
	{
		super("data-load/clients.csv", new String[]{"name", "email"}, "loadClients");
		this.clientService = clientService;
	}

	@Bean
	public FlatFileItemReader<CreateClientRecord> clientItemReader()
	{
		return super.itemReader();
	}

	@Bean
	public BeanValidatingItemProcessor<CreateClientRecord> clientValidatingProcessor()
	{
		return super.validatingProcessor();
	}

	@Bean
	public ItemWriter<CreateClientRecord> clientItemWriter()
	{
		return super.itemWriter(clientService::createClient);
	}

	@Bean
	public Step readClientFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<CreateClientRecord> clientItemReader, BeanValidatingItemProcessor<CreateClientRecord> clientValidatingProcessor, ItemWriter<CreateClientRecord> clientItemWriter)
	{
		return super.readFromCsv(repository, transactionManager, clientItemReader, clientValidatingProcessor, clientItemWriter);
	}

	@Bean
	public Job loadClientsFromCsv(JobRepository repository, Step readClientFromCsv)
	{
		return super.loadFromCsv(repository, readClientFromCsv);
	}

	@Override
	protected FieldSetMapper<CreateClientRecord> fieldSetMapper()
	{
		return fieldSet -> new CreateClientRecord(fieldSet.readString("name"), fieldSet.readString("email"));
	}
}
