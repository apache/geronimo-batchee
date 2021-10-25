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

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BatchEEUITest {
    @Test
    public void ensureUIRuns() {
        try (final ConfigurableApplicationContext ctx = runBatch()) {
            assertUIIsThere(ctx);
        }
    }

    private ConfigurableApplicationContext runBatch() {
        final SpringApplication app = new SpringApplication(BatchApp.class);
        final AtomicReference<JobExecution> done = new AtomicReference<>(null);
        final AtomicReference<ConfigurableApplicationContext> context = new AtomicReference<>(null);
        app.addListeners(applicationEvent -> {
            if (!ApplicationReadyEvent.class.isInstance(applicationEvent)) {
                return;
            }

            final ConfigurableApplicationContext ctx = ApplicationReadyEvent.class.cast(applicationEvent)
                    .getApplicationContext();
            context.set(ctx);
            try {
                done.set(ctx.getBean(JobLauncher.class).run(ctx.getBean(Job.class), new JobParameters()));
                ctx.getBean(Awaiter.class).latch.await();
            } catch (final JobExecutionAlreadyRunningException | JobRestartException |
                    JobInstanceAlreadyCompleteException | JobParametersInvalidException |
                    InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
        app.run("--server.port=0", "--server.tomcat.basedir=target/tomcat");

        final JobExecution exec = done.get();
        assertNotNull(exec);

        final JobExecution jobExecution = context.get().getBean(JobExplorer.class).getJobExecution(exec.getJobId());
        assertNotNull(jobExecution);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        return context.get();
    }

    // snaity checks to ensure the UI is properly rendered
    private void assertUIIsThere(final ConfigurableApplicationContext ctx) {
        final int port = ServletWebServerApplicationContext.class.cast(ctx).getWebServer().getPort();
        { // home
            final ResponseEntity<String> home = new RestTemplate().getForEntity("http://localhost:" + port + "/batchee/", String.class);
            assertEquals(200, home.getStatusCodeValue());
            final String body = home.getBody();
            assertNotNull(body);
            assertTrue(body, body.contains("<h3 class=\"text-muted\">Apache JBatch GUI</h3>"));
            assertTrue(body, body.contains("<a href=\"/batchee/executions/BatchEEUITestBatch\">BatchEEUITestBatch</a>"));
        }
        { // detail for the batch
            final ResponseEntity<String> home = new RestTemplate().getForEntity("http://localhost:" + port + "/batchee/executions/BatchEEUITestBatch", String.class);
            assertEquals(200, home.getStatusCodeValue());
            final String body = home.getBody();
            assertNotNull(body);
            assertTrue(body, body.contains("<td>COMPLETED</td>"));
            assertTrue(body, body.contains("<td><a href=\"/batchee/step-executions/1\">1</a></td>"));
        }
    }

    @EnableBatchProcessing
    @SpringBootApplication
    public static class BatchApp {
        @Bean(destroyMethod = "close")
        public DataSource dataSource() {
            final HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:derby:memory:BatchEEUITest;create=true");
            dataSource.setUsername("SA");
            dataSource.setPassword("");
            dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
            return dataSource;
        }

        @Bean
        public Awaiter awaiter() {
            return new Awaiter();
        }

        @Bean
        public Job job(final JobBuilderFactory jobBuilderFactory,
                       final StepBuilderFactory stepBuilderFactory,
                       final Awaiter awaiter) {
            return jobBuilderFactory
                    .get("BatchEEUITestBatch")
                    .listener(new JobExecutionListenerSupport() {
                        @Override
                        public void afterJob(final JobExecution jobExecution) {
                            awaiter.latch.countDown();
                        }
                    })
                    .start(stepBuilderFactory.get("step1").tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED).build())
                    .build();
        }
    }

    public static class Awaiter {
        private final CountDownLatch latch = new CountDownLatch(1);
    }
}
