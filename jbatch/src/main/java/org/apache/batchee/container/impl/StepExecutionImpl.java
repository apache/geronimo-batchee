/*
 * Copyright 2012 International Business Machines Corp.
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.apache.batchee.container.impl;

import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.Metric;
import jakarta.batch.runtime.StepExecution;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class StepExecutionImpl implements StepExecution, Serializable {

    private long commitCount = 0;
    private Timestamp endTime = null;
    private String exitStatus = null;
    private BatchStatus batchStatus = null;

    private long filterCount = 0;
    private long jobExecutionId = 0;
    private long processSkipCount = 0;
    private long readCount = 0;
    private long readSkipCount = 0;
    private long rollbackCount = 0;
    private Timestamp startTime = null;
    private long stepExecutionId = 0;
    private String stepName = null;

    private long writeCount = 0;
    private long writeSkipCount = 0;

    private Serializable persistentUserData = null;

    private StepContextImpl stepContext = null;

    public StepExecutionImpl(long jobExecutionId, long stepExecutionId) {
        this.jobExecutionId = jobExecutionId;
        this.stepExecutionId = stepExecutionId;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public long getStepExecutionId() {
        return this.stepExecutionId;
    }

    @Override
    public Date getEndTime() {
        if (stepContext != null) {
            return this.stepContext.getEndTimeTS();
        } else {
            if (endTime != null) {
                return new Date(endTime.getTime());
            } else {
                return null;
            }
        }
    }

    @Override
    public String getExitStatus() {
        if (stepContext != null) {
            return this.stepContext.getExitStatus();
        } else {
            return exitStatus;
        }
    }

    @Override
    public Date getStartTime() {
        if (stepContext != null) {
            return this.stepContext.getStartTimeTS();
        } else {
            if (startTime != null) {
                return new Date(startTime.getTime());
            } else {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        //buf.append("getCommitCount(): " + this.getCommitCount() + "\n");
        //buf.append("getFilterCount(): " + this.getFilterCount() + "\n");
        //buf.append("getProcessSkipCount(): " + this.getProcessSkipCount() + "\n");
        //buf.append("getReadCount(): " + this.getReadCount() + "\n");
        //buf.append("getReadSkipCount(): " + this.getReadSkipCount() + "\n");
        //buf.append("getRollbackCount(): " + this.getRollbackCount() + "\n");
        //buf.append("getWriteCount(): " + this.getWriteCount() + "\n");
        //buf.append("getWriteSkipCount(): " + this.getWriteSkipCount() + "\n");
        //buf.append("getLastUpdateTime(): " + this.getLastUpdateTime() + "\n");
        return "---------------------------------------------------------------------------------\n"
            + "getStepName(): " + this.getStepName() + "\n"
            + "getStepExecutionId(): " + this.stepExecutionId + "\n"
            + "getJobExecutionId(): " + this.jobExecutionId + "\n"
            + "getStartTime(): " + this.getStartTime() + "\n"
            + "getEndTime(): " + this.getEndTime() + "\n"
            + "getBatchStatus(): " + (getBatchStatus() != null ? this.getBatchStatus().name() : "-") + "\n"
            + "getExitStatus(): " + this.getExitStatus() + "\n"
            + "---------------------------------------------------------------------------------\n";
    }

    @Override
    public Metric[] getMetrics() {


        if (stepContext != null) {
            return stepContext.getMetrics();
        } else {
            Metric[] metrics = new MetricImpl[8];
            metrics[0] = new MetricImpl(MetricImpl.MetricType.READ_COUNT, readCount);
            metrics[1] = new MetricImpl(MetricImpl.MetricType.WRITE_COUNT, writeCount);
            metrics[2] = new MetricImpl(MetricImpl.MetricType.COMMIT_COUNT, commitCount);
            metrics[3] = new MetricImpl(MetricImpl.MetricType.ROLLBACK_COUNT, rollbackCount);
            metrics[4] = new MetricImpl(MetricImpl.MetricType.READ_SKIP_COUNT, readSkipCount);
            metrics[5] = new MetricImpl(MetricImpl.MetricType.PROCESS_SKIP_COUNT, processSkipCount);
            metrics[6] = new MetricImpl(MetricImpl.MetricType.FILTER_COUNT, filterCount);
            metrics[7] = new MetricImpl(MetricImpl.MetricType.WRITE_SKIP_COUNT, writeSkipCount);

            return metrics;
        }
    }

    @Override
    public BatchStatus getBatchStatus() {

        if (stepContext != null) {
            return this.stepContext.getBatchStatus();
        } else {
            return batchStatus;
        }
    }

    @Override
    public Serializable getPersistentUserData() {
        if (stepContext != null) {
            return this.stepContext.getPersistentUserData();
        } else {
            return this.persistentUserData;
        }
    }


    // impl specific setters
    public void setFilterCount(long filterCnt) {
        this.filterCount = filterCnt;
    }

    public void setProcessSkipCount(long processSkipCnt) {
        this.processSkipCount = processSkipCnt;
    }

    public void setReadCount(long readCnt) {
        this.readCount = readCnt;
    }

    public void setReadSkipCount(long readSkipCnt) {
        this.readSkipCount = readSkipCnt;
    }

    public void setRollbackCount(long rollbackCnt) {
        this.rollbackCount = rollbackCnt;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setWriteCount(long writeCnt) {
        this.writeCount = writeCnt;
    }

    public void setWriteSkipCount(long writeSkipCnt) {
        this.writeSkipCount = writeSkipCnt;
    }

    public void setStepContext(StepContextImpl stepContext) {
        this.stepContext = stepContext;
    }

    public void setCommitCount(long commitCnt) {
        this.commitCount = commitCnt;
    }

    public void setBatchStatus(BatchStatus batchstatus) {
        this.batchStatus = batchstatus;
    }

    public void setExitStatus(String exitstatus) {
        this.exitStatus = exitstatus;
    }

    public void setStartTime(Timestamp startts) {
        this.startTime = startts;
    }

    public void setEndTime(Timestamp endts) {
        this.endTime = endts;
    }

    public void setPersistentUserData(Serializable data) {
        this.persistentUserData = data;
    }

    @Override
    public String getStepName() {
        if (stepContext != null) {
            return this.stepContext.getStepName();
        }
        return stepName;
    }
}
