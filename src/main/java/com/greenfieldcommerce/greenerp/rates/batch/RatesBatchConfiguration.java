package com.greenfieldcommerce.greenerp.rates.batch;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;

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
import com.greenfieldcommerce.greenerp.rates.records.BatchContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.records.ContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.records.CreateContractorRateRecord;
import com.greenfieldcommerce.greenerp.rates.services.ContractorRateService;

@Configuration
public class RatesBatchConfiguration extends AbstractBatchConfiguration<BatchContractorRateRecord, ContractorRateRecord>
{
	private final ContractorRateService contractorRateService;

	public RatesBatchConfiguration(final ContractorRateService contractorRateService)
	{
		super("data-load/rates.csv", new String[] { "contractorId", "clientId", "externalRate", "taxDeduction", "rate", "currency", "startDateTime", "endDateTime" }, "readRates");
		this.contractorRateService = contractorRateService;
	}

	@Bean
	public FlatFileItemReader<BatchContractorRateRecord> contractorRateItemReader()
	{
		return super.itemReader();
	}

	@Bean
	public BeanValidatingItemProcessor<BatchContractorRateRecord> contractorRateValidationProcessor()
	{
		return super.validatingProcessor();
	}

	@Bean
	public ItemWriter<BatchContractorRateRecord> contractorRateItemWriter()
	{
		return super.itemWriter(record -> contractorRateService.create(record.contractorId(), record.rate()));
	}

	@Bean
	public Step readContractorRateFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<BatchContractorRateRecord> contractorRateItemReader, ValidatingItemProcessor<BatchContractorRateRecord> contractorRateValidationProcessor, ItemWriter<BatchContractorRateRecord> contractorRateItemWriter)
	{
		return super.readFromCsv(repository, transactionManager, contractorRateItemReader, contractorRateValidationProcessor, contractorRateItemWriter);
	}

	@Bean
	public Job loadContractorRatesFromCsv(JobRepository repository, Step readContractorRateFromCsv)
	{
		return super.loadFromCsv(repository, readContractorRateFromCsv);
	}

	@Override
	protected FieldSetMapper<BatchContractorRateRecord> fieldSetMapper()
	{
		return fieldSet -> new BatchContractorRateRecord(
			fieldSet.readLong("contractorId"),
			new CreateContractorRateRecord(fieldSet.readLong("clientId"),
				fieldSet.readBigDecimal("rate"),
				fieldSet.readBigDecimal("externalRate"),
				fieldSet.readBigDecimal("taxDeduction"), Currency.getInstance(fieldSet.readString("currency")),
				ZonedDateTime.ofInstant(fieldSet.readDate("startDateTime").toInstant(), ZoneId.systemDefault()),
				ZonedDateTime.ofInstant(fieldSet.readDate("endDateTime").toInstant(), ZoneId.systemDefault())));
	}

}
