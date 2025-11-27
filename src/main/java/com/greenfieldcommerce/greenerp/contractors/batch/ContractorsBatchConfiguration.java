package com.greenfieldcommerce.greenerp.contractors.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.greenfieldcommerce.greenerp.batch.AbstractBatchConfiguration;
import com.greenfieldcommerce.greenerp.contractors.records.ContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;

@Configuration
public class ContractorsBatchConfiguration extends AbstractBatchConfiguration<CreateContractorRecord, ContractorRecord>
{

	private final ContractorService contractorService;

	public ContractorsBatchConfiguration(final ContractorService contractorService)
	{
		super("data-load/contractors.csv", new String[] { "email", "name" }, "readContractors");
		this.contractorService = contractorService;
	}

	@Bean
	public FlatFileItemReader<CreateContractorRecord> contractorItemReader()
	{
		return super.itemReader();
	}

	@Bean
	public BeanValidatingItemProcessor<CreateContractorRecord> contractorValidatingProcessor()
	{
		return super.validatingProcessor();
	}

	@Bean
	public ItemWriter<CreateContractorRecord> contractorItemWriter()
	{
		return super.itemWriter(contractorService::create);
	}

	@Bean
	public Step readContractorFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<CreateContractorRecord> contractorItemReader, ValidatingItemProcessor<CreateContractorRecord> contractorValidatingProcessor, ItemWriter<CreateContractorRecord> contractorItemWriter)
	{
		return super.readFromCsv(repository, transactionManager, contractorItemReader, contractorValidatingProcessor, contractorItemWriter);
	}

	@Bean
	public Job loadContractorsFromCsv(JobRepository repository, Step readContractorFromCsv)
	{
		return super.loadFromCsv(repository, readContractorFromCsv);
	}

	@Override
	protected FieldSetMapper<CreateContractorRecord> fieldSetMapper()
	{
		return fieldSet -> new CreateContractorRecord(fieldSet.readString("email"), fieldSet.readString("name"));
	}

}
