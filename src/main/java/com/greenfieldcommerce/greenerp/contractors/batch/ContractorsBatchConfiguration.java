package com.greenfieldcommerce.greenerp.contractors.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.greenfieldcommerce.greenerp.contractors.records.CreateContractorRecord;
import com.greenfieldcommerce.greenerp.contractors.services.ContractorService;

@Configuration
public class ContractorsBatchConfiguration
{

	private final ContractorService contractorService;

	public ContractorsBatchConfiguration(final ContractorService contractorService)
	{
		this.contractorService = contractorService;
	}

	@Bean
	public FlatFileItemReader<CreateContractorRecord> itemReader()
	{
		return new FlatFileItemReaderBuilder<CreateContractorRecord>()
			.name("contractorReader")
			.resource(new ClassPathResource("data-load/contractors.csv"))
			.delimited().delimiter(",").names("email", "name")
			.linesToSkip(1)
			.fieldSetMapper(fieldSet -> new CreateContractorRecord(fieldSet.readString("email"), fieldSet.readString("name")))
			.build();
	}

	@Bean
	public BeanValidatingItemProcessor<CreateContractorRecord> validatingProcessor() {
		BeanValidatingItemProcessor<CreateContractorRecord> processor = new BeanValidatingItemProcessor<>();
		processor.setFilter(false);
		return processor;
	}

	@Bean
	public ItemWriter<CreateContractorRecord> itemWriter()
	{
		return items -> items.forEach(contractorService::create);
	}

	@Bean
	public Step readFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<CreateContractorRecord> itemReader, ValidatingItemProcessor<CreateContractorRecord> validatingProcessor, ItemWriter<CreateContractorRecord> itemWriter)
	{
		return new StepBuilder("readContractorFromCsv", repository)
			.<CreateContractorRecord, CreateContractorRecord>chunk(1, transactionManager)
			.reader(itemReader)
			.processor(validatingProcessor)
			.writer(itemWriter)
			.build();
	}

	@Bean
	public Job loadContractorsFromCsv(JobRepository repository, Step readFromCsV)
	{
		return new JobBuilder("readContractors", repository).start(readFromCsV).build();
	}

}
