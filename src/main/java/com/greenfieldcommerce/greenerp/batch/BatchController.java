package com.greenfieldcommerce.greenerp.batch;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenfieldcommerce.greenerp.security.AuthenticationConstraint;

@RestController
@RequestMapping("/batch")
public class BatchController
{
	private final JobLauncher jobLauncher;
	private final Job readContractorsFromCSVJob;

	public BatchController(final JobLauncher jobLauncher, final Job readContractorsFromCSVJob)
	{
		this.jobLauncher = jobLauncher;
		this.readContractorsFromCSVJob = readContractorsFromCSVJob;
	}

	@PostMapping("/contractors")
	@PreAuthorize(AuthenticationConstraint.ALLOW_ADMIN_ONLY)
	public ResponseEntity<String> startContractorsBatch()
	{
		JobParameters jobParameters = new JobParametersBuilder().addLong("run.id", System.currentTimeMillis())
			.toJobParameters();

		try
		{
			jobLauncher.run(readContractorsFromCSVJob, jobParameters);
		}
		catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e )
		{
			return ResponseEntity.status(500).body("Job failed: " + e.getMessage());
		}

		return ResponseEntity.ok("Batch started");
	}
}
