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
import org.apache.catalina.WebResourceRoot;
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
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;
import static org.apache.catalina.Lifecycle.CONFIGURE_START_EVENT;
import static org.apache.catalina.WebResourceRoot.ResourceSetType.RESOURCE_JAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BatchEEUITest {
    @Test
    public void ensureUIRuns() throws InterruptedException {
        try (final ConfigurableApplicationContext ctx = runBatch()) {
            final int maxRetries = 10;
            for (int i = 1; i <= maxRetries; i++) {
                try {
                    assertUIIsThere(ctx);
                    break;
                } catch (final HttpClientErrorException.NotFound ae) {
                    if (i == maxRetries) {
                        throw ae;
                    }
                    sleep(250);
                }
            }
        }
    }

    private ConfigurableApplicationContext runBatch() {
        final SpringApplication app = new SpringApplication(BatchApp.class);
        final JobExecution done;
        final ConfigurableApplicationContext ctx = app.run("--server.port=0", "--server.tomcat.basedir=target/tomcat");
        try {
            done = ctx.getBean(JobLauncher.class).run(ctx.getBean(Job.class), new JobParameters());
            ctx.getBean(Awaiter.class).latch.await();
        } catch (final JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException |
                InterruptedException e) {
            throw new IllegalStateException(e);
        }

        final JobExecution jobExecution = ctx.getBean(JobExplorer.class).getJobExecution(done.getJobId());
        assertNotNull(jobExecution);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        return ctx;
    }

    // sanity checks to ensure the UI is properly rendered
    private void assertUIIsThere(final ConfigurableApplicationContext ctx) {
        final int port = ServletWebServerApplicationContext.class.cast(ctx).getWebServer().getPort();
        assertHome(port);
        assertDetails(port);
    }

    private void assertDetails(final int port) {
        final ResponseEntity<String> home = new RestTemplate().getForEntity("http://localhost:" + port + "/batchee/executions/BatchEEUITestBatch", String.class);
        assertEquals(200, home.getStatusCodeValue());
        final String body = home.getBody();
        assertNotNull(body);
        assertTrue(body, body.contains("<td>COMPLETED</td>"));
        assertTrue(body, body.contains("<td><a href=\"/batchee/step-executions/1\">1</a></td>"));
    }

    private void assertHome(final int port) {
        final ResponseEntity<String> home = new RestTemplate().getForEntity("http://localhost:" + port + "/batchee/", String.class);
        assertEquals(200, home.getStatusCodeValue());
        final String body = home.getBody();
        assertNotNull(body);
        assertTrue(body, body.contains("<h3 class=\"text-muted\">Apache JBatch GUI</h3>"));
        assertTrue(body, body.contains("<a href=\"/batchee/executions/BatchEEUITestBatch\">BatchEEUITestBatch</a>"));
    }

    @EnableBatchProcessing
    @SpringBootApplication
    public static class BatchApp {
        @Bean // StaticResourceJars fails with surefire classloader so let's workaround it for the test
        public TomcatContextCustomizer surefireStaticResourceJarsWorkaround() {
            return context -> context.addLifecycleListener(event -> {
                if (!event.getType().equals(CONFIGURE_START_EVENT)) {
                    return;
                }

                final URL url = Thread.currentThread().getContextClassLoader()
                        .getResource("META-INF/resources/internal/batchee/layout.jsp");
                final WebResourceRoot resources = context.getResources();
                try {
                    final URL cleanUrl = new URL(
                            url.getProtocol(), url.getHost(), url.getPort(),
                            url.getFile().substring(0, url.getFile().length() - "META-INF/resources/internal/batchee/layout.jsp".length()));
                    resources.createWebResourceSet(RESOURCE_JAR, "/", cleanUrl, "/META-INF/resources");
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            });
        }

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
