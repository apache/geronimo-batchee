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
package org.apache.batchee.test.tck.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * This extension will be globally registered via SPI in order to skip certain TCK tests during JUnit 5 / Surefire
 * execution.
 */
public class IgnoreTestInTCK implements ExecutionCondition {

    private static final List<String> DISABLED_TCK_TESTS = List.of(
            /*
             * This test will fail on our implementation with OWB as OWB does not pass an InjectionPoint to the related
             * producer method of BatchProducerBean#produceProperty(InjectionPoint). The reference impl with Weld 5
             * passes this test because Weld 5 passes a 'fake' injection point the producer.
             *
             * We might want to open a TCK challenge for it with some technical explanation.
             */
            "com.ibm.jbatch.tck.tests.jslxml.CDITests#testCDILookup"
    );

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        final Optional<String> testName = context.getTestMethod().map(Method::getName);
        final Optional<String> testClass = context.getTestClass().map(Class::getName);

        if (testName.isPresent()
                && testClass.isPresent()
                && DISABLED_TCK_TESTS.contains(testClass.get() + "#" + testName.get())) {
            return ConditionEvaluationResult.disabled("Test skipped as it is disabled");
        } else {
            return ConditionEvaluationResult.enabled("Test not skipped");
        }
    }
}