package org.apache.batchee.container.impl;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.runtime.JobInstance;

import org.apache.batchee.container.services.InternalJobExecution;
import org.apache.batchee.container.services.ServicesManager;
import org.apache.batchee.container.services.persistence.JPAPersistenceManagerService;
import org.apache.batchee.spi.PersistenceManagerService;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("serial")
public class JPAPersistenceManagerTest {

	private static final String VALID_JOBNAME = "simple";
	private static final String INVALID_JOBNAME = "simple_batchee139";
	private static final int INVALID_ID = -1;
	private static final Properties simpleJobProp;

	private static JobOperatorImpl operator;
	private static long executionId;

	static {
		simpleJobProp = new Properties() {
			{
				setProperty("duration", "10");
			}
		};
	}

	@BeforeClass
	public static void setup() {
		operator = new JobOperatorImpl(new ServicesManager() {
			{
				init(new Properties() {
					{
						setProperty(PersistenceManagerService.class.getName(),
								JPAPersistenceManagerService.class.getName());
					}
				});
			}
		});
		executionId = triggerSimpleJob();
	}

	@Test(expected = NoSuchJobExecutionException.class)
	public void testGetJobExecutionError_BATCHEE139() {
		operator.getJobExecution(INVALID_ID);
	}

	@Test
	public void testGetJobExecution_BATCHEE139() {
		final InternalJobExecution jobExecution = operator.getJobExecution(executionId);
		assertEquals(executionId, jobExecution.getExecutionId());
	}

	@Test(expected = NoSuchJobExecutionException.class)
	public void testGetJobInstanceError_BATCHEE139() {
		operator.getJobInstance(INVALID_ID);
	}

	@Test
	public void testGetJobInstance_BATCHEE139() {
		final JobInstance jobInstance = operator.getJobInstance(executionId);
		assertEquals(operator.getJobExecution(executionId).getInstanceId(), jobInstance.getInstanceId());
	}

	@Test
	public void testGetParameters_BATCHEE139() {
		final Properties parameters = operator.getParameters(executionId);
		assertEquals(simpleJobProp, parameters);
	}

	@Test(expected = NoSuchJobExecutionException.class)
	public void testGetParametersError_BATCHEE139() {
		operator.getParameters(INVALID_ID);
	}

	@Test(expected = NoSuchJobException.class)
	public void testJobInstanceCountError_BATCHEE139() {
		operator.getJobInstanceCount(INVALID_JOBNAME);
	}

	private static long triggerSimpleJob() {
		return operator.start(VALID_JOBNAME, simpleJobProp);
	}

}
