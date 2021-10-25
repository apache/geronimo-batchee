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

import org.apache.batchee.servlet.JBatchController;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.jsr.JsrJobParametersConverter;
import org.springframework.batch.core.jsr.launch.JsrJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.batch.operations.JobOperator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class BatchEEUI {
    @Bean
    @ConditionalOnMissingBean(value = JobOperator.class)
    public JobOperator operator(final JobExplorer jobExplorer,
                                final JobRepository jobRepository,
                                final PlatformTransactionManager transactionManager,
                                final DataSource dataSource,
                                final Properties properties,
                                @Autowired(required = false) final List<Job> jobs) throws Exception {
        final JsrJobParametersConverter jobParametersConverter = new JsrJobParametersConverter(dataSource);
        jobParametersConverter.afterPropertiesSet();
        return new BatchEEJobOperator(
                new JsrJobOperator(jobExplorer, jobRepository, jobParametersConverter, transactionManager),
                getJobNames(properties, jobs));
    }

    @Bean
    public ServletRegistrationBean<JBatchController> batcheeUI(final JobOperator operator, final Properties properties) {
        final ServletRegistrationBean<JBatchController> registrationBean = new ServletRegistrationBean<>(new JBatchController() {
            @Override
            public void init(final ServletConfig config) {
                // force operator to be this one since spring JsrJobOperator loads an "external" context we don't want at all
                BatchEEJobOperator.Context.withFacade(operator, op -> {
                    try {
                        super.init(config);
                        return null;
                    } catch (final ServletException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
        }.defaultScan(false).mapping(properties.getMapping()), properties.getMapping());
        registrationBean.setLoadOnStartup(1);
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    private String[] getJobNames(final Properties properties, final List<Job> jobs) {
        try {
            return jobs != null && properties.getDefaultBatchNames().length == 0 ?
                    jobs.stream().map(Job::getName).toArray(String[]::new) :
                    properties.getDefaultBatchNames();
        } catch (final RuntimeException re) {
            return properties.getDefaultBatchNames();
        }
    }

    @Configuration
    @ConfigurationProperties(prefix = "batchee.ui")
    public static class Properties {
        private String mapping = "/batchee/*";
        private String[] defaultBatchNames = new String[0];

        public String[] getDefaultBatchNames() {
            return defaultBatchNames;
        }

        public void setDefaultBatchNames(final String[] defaultBatchNames) {
            this.defaultBatchNames = defaultBatchNames;
        }

        public void setMapping(final String mapping) {
            this.mapping = mapping;
        }

        public String getMapping() {
            return mapping;
        }
    }
}
