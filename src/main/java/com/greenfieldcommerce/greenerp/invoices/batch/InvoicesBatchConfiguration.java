package com.greenfieldcommerce.greenerp.invoices.batch;

import java.time.ZoneId;
import java.time.ZonedDateTime;

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
import com.greenfieldcommerce.greenerp.invoices.records.BatchContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.records.ContractorInvoiceRecord;
import com.greenfieldcommerce.greenerp.invoices.services.ContractorInvoiceService;

@Configuration
public class InvoicesBatchConfiguration extends AbstractBatchConfiguration<BatchContractorInvoiceRecord, ContractorInvoiceRecord>
{

	private final ContractorInvoiceService contractorInvoiceService;

	protected InvoicesBatchConfiguration(final ContractorInvoiceService contractorInvoiceService)
	{
		super("data-load/invoices.csv", new String[] { "contractorId", "clientId", "startDate", "endDate", "numberOfWorkedDays"}, "readInvoices");
		this.contractorInvoiceService = contractorInvoiceService;
	}

	@Bean
	public FlatFileItemReader<BatchContractorInvoiceRecord> contractorInvoiceItemReader()
	{
		return super.itemReader();
	}

	@Bean
	public BeanValidatingItemProcessor<BatchContractorInvoiceRecord> contractorInvoiceValidationProcessor()
	{
		return super.validatingProcessor();
	}

	@Bean
	public ItemWriter<BatchContractorInvoiceRecord> contractorInvoiceItemWriter()
	{
		return super.itemWriter(contractorInvoiceService::create);
	}

	@Bean
	public Step readContractorInvoiceFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<BatchContractorInvoiceRecord> contractorInvoiceItemReader, ValidatingItemProcessor<BatchContractorInvoiceRecord> contractorInvoiceValidationProcessor, ItemWriter<BatchContractorInvoiceRecord> contractorInvoiceItemWriter)
	{
		return super.readFromCsv(repository, transactionManager, contractorInvoiceItemReader, contractorInvoiceValidationProcessor, contractorInvoiceItemWriter);
	}

	@Bean
	public Job loadContractorInvoicesFromCsv(JobRepository repository, Step readContractorInvoiceFromCsv)
	{
		return super.loadFromCsv(repository, readContractorInvoiceFromCsv);
	}

	@Override
	protected FieldSetMapper<BatchContractorInvoiceRecord> fieldSetMapper()
	{
		return fieldSet -> new BatchContractorInvoiceRecord(
			fieldSet.readLong("contractorId"),
			fieldSet.readLong("clientId"),
			ZonedDateTime.ofInstant(fieldSet.readDate("startDate").toInstant(), ZoneId.systemDefault()),
			ZonedDateTime.ofInstant(fieldSet.readDate("endDate").toInstant(), ZoneId.systemDefault()),
			fieldSet.readBigDecimal("numberOfWorkedDays"));
	}
}
