/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.batchee.spring.ui;

import javax.batch.operations.JobExecutionAlreadyCompleteException;
import javax.batch.operations.JobExecutionIsRunningException;
import javax.batch.operations.JobExecutionNotMostRecentException;
import javax.batch.operations.JobExecutionNotRunningException;
import javax.batch.operations.JobOperator;
import javax.batch.operations.JobRestartException;
import javax.batch.operations.JobSecurityException;
import javax.batch.operations.JobStartException;
import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.operations.NoSuchJobInstanceException;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import javax.batch.runtime.StepExecution;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class DelegatingJobOperator implements JobOperator {
    protected final JobOperator delegate;

    protected DelegatingJobOperator(final JobOperator operator) {
        this.delegate = operator;
    }

    @Override
    public Set<String> getJobNames() throws JobSecurityException {
        return delegate.getJobNames();
    }

    @Override
    public int getJobInstanceCount(final String s) throws NoSuchJobException, JobSecurityException {
        return delegate.getJobInstanceCount(s);
    }

    @Override
    public List<JobInstance> getJobInstances(final String s, final int i, final int i1) throws NoSuchJobException, JobSecurityException {
        return delegate.getJobInstances(s, i, i1);
    }

    @Override
    public List<Long> getRunningExecutions(final String s) throws NoSuchJobException, JobSecurityException {
        return delegate.getRunningExecutions(s);
    }

    @Override
    public Properties getParameters(final long l) throws NoSuchJobExecutionException, JobSecurityException {
        return delegate.getParameters(l);
    }

    @Override
    public long start(final String s, final Properties properties) throws JobStartException, JobSecurityException {
        return delegate.start(s, properties);
    }

    @Override
    public long restart(final long l, final Properties properties)
            throws JobExecutionAlreadyCompleteException, NoSuchJobExecutionException,
            JobExecutionNotMostRecentException, JobRestartException, JobSecurityException {
        return delegate.restart(l, properties);
    }

    @Override
    public void stop(final long l) throws NoSuchJobExecutionException, JobExecutionNotRunningException, JobSecurityException {
        delegate.stop(l);
    }

    @Override
    public void abandon(final long l) throws NoSuchJobExecutionException, JobExecutionIsRunningException, JobSecurityException {
        delegate.abandon(l);
    }

    @Override
    public JobInstance getJobInstance(final long l) throws NoSuchJobExecutionException, JobSecurityException {
        return delegate.getJobInstance(l);
    }

    @Override
    public List<JobExecution> getJobExecutions(final JobInstance jobInstance) throws NoSuchJobInstanceException, JobSecurityException {
        return delegate.getJobExecutions(jobInstance);
    }

    @Override
    public JobExecution getJobExecution(final long l) throws NoSuchJobExecutionException, JobSecurityException {
        return delegate.getJobExecution(l);
    }

    @Override
    public List<StepExecution> getStepExecutions(final long l) throws NoSuchJobExecutionException, JobSecurityException {
        return delegate.getStepExecutions(l);
    }
}
