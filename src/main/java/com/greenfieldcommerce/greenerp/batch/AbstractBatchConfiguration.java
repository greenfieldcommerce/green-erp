package com.greenfieldcommerce.greenerp.batch;

import java.util.function.Function;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class AbstractBatchConfiguration<I, O>
{
	private final String fileName;
	private final String[] columnNames;
	private final String jobName;

	protected AbstractBatchConfiguration(final String fileName, final String[] columnNames, final String jobName)
	{
		this.fileName = fileName;
		this.columnNames = columnNames;
		this.jobName = jobName;
	}

	public FlatFileItemReader<I> itemReader()
	{
		return new FlatFileItemReaderBuilder<I>()
			.name("reader")
			.resource(new ClassPathResource(fileName))
			.delimited().delimiter(",").names(columnNames)
			.linesToSkip(1)
			.fieldSetMapper(fieldSetMapper())
			.build();
	}

	public BeanValidatingItemProcessor<I> validatingProcessor() {
		BeanValidatingItemProcessor<I> processor = new BeanValidatingItemProcessor<>();
		processor.setFilter(false);
		return processor;
	}

	public ItemWriter<I> itemWriter(Function<I, O> builder)
	{
		return items -> items.forEach(i -> builder.apply(i));
	}

	public Step readFromCsv(JobRepository repository, PlatformTransactionManager transactionManager, FlatFileItemReader<I> itemReader, ValidatingItemProcessor<I> validatingProcessor, ItemWriter<I> itemWriter)
	{
		return new StepBuilder("readFromCsv", repository)
			.<I, I>chunk(1, transactionManager)
			.reader(itemReader)
			.processor(validatingProcessor)
			.writer(itemWriter)
			.build();
	}

	public Job loadFromCsv(JobRepository repository, Step readFromCsV)
	{
		return new JobBuilder(jobName, repository).start(readFromCsV).build();
	}

	protected abstract FieldSetMapper<I> fieldSetMapper();

}
