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

import jakarta.batch.runtime.JobInstance;
import java.io.Serializable;

public class JobInstanceImpl implements JobInstance, Serializable {
    private static final long serialVersionUID = 1L;

    private long jobInstanceId = 0L;
    private String jobName = null;
    private String jobXML = null;

    private JobInstanceImpl() {
        // no-op
    }

    public JobInstanceImpl(long instanceId) {
        this.jobInstanceId = instanceId;
    }

    public JobInstanceImpl(long instanceId, String jobXML) {
        this.jobXML = jobXML;
        this.jobInstanceId = instanceId;
    }

    @Override
    public long getInstanceId() {
        return jobInstanceId;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    public String getJobXML() {
        return jobXML;
    }


    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(" jobName: ").append(jobName);
        buf.append(" jobInstance id: ").append(jobInstanceId);
        if (jobXML != null) {
            int concatLen = jobXML.length() > 300 ? 300 : jobXML.length();
            buf.append(" jobXML: ").append(jobXML.subSequence(0, concatLen)).append("...truncated ...\n");
        } else {
            buf.append(" jobXML = null");
        }
        return buf.toString();
    }

}
