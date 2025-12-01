package com.greenfieldcommerce.greenerp.batch;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

@RestController
@RequestMapping("/batch")
public class BatchController
{
	private final JobLauncher jobLauncher;
	private final Job loadContractorsFromCsv;
	private final Job loadClientsFromCsv;
	private final Job loadContractorRatesFromCsv;

	private final Map<String, Job> jobs;

	public BatchController(final JobLauncher jobLauncher, final Job loadContractorsFromCsv, final Job loadClientsFromCsv, final Job loadContractorRatesFromCsv)
	{
		this.jobLauncher = jobLauncher;
		this.loadContractorsFromCsv = loadContractorsFromCsv;
		this.loadClientsFromCsv = loadClientsFromCsv;
		this.loadContractorRatesFromCsv = loadContractorRatesFromCsv;

		jobs = Map.of("clients", loadClientsFromCsv, "contractors", loadContractorsFromCsv, "rates", loadContractorRatesFromCsv);
	}

	@PostMapping("/{jobName}")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<String> startContractorsBatch(@PathVariable final String jobName)
	{
		JobParameters jobParameters = new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters();

		try
		{
			final Job job = jobs.get(jobName);
			jobLauncher.run(job, jobParameters);
		}
		catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e )
		{
			return ResponseEntity.status(500).body("Job failed: " + e.getMessage());
		}

		return ResponseEntity.ok("Batch started");
	}

}
