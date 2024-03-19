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
package org.apache.batchee.container.services.factory;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import org.apache.batchee.container.cdi.BatchCDIInjectionExtension;
import org.apache.batchee.container.exception.BatchContainerServiceException;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class CDIBatchArtifactFactory extends DefaultBatchArtifactFactory {

    @Override
    public Instance load(final String batchId) {
        final BeanManager bm = getBeanManager();
        if (bm == null) {
            return super.load(batchId);
        }

        final Bean<?> bean = getBeanById(bm, batchId);

        if (bean == null) { // fallback to try to instantiate it from TCCL as per the spec
            return super.load(batchId);
        }
        final Class<?> clazz = bean.getBeanClass();
        final CreationalContext creationalContext = bm.createCreationalContext(bean);
        final Object artifactInstance = bm.getReference(bean, clazz, creationalContext);
        if (Dependent.class.equals(bean.getScope()) || !bm.isNormalScope(bean.getScope())) { // need to be released
            return new Instance(artifactInstance, new Closeable() {
                @Override
                public void close() throws IOException {
                    creationalContext.release();
                }
            });
        }
        return new Instance(artifactInstance, null);
    }

    /**
     * @param id Either the EL name of the bean, its id in batch.xml, or its fully qualified class name.
     *
     * @return the bean for the given artifact id.
     */
    protected Bean<?> getBeanById(BeanManager bm, String id) {

        Bean<?> match = getUniqueBeanByBeanName(bm, id);

        if (match == null) {
            match = getUniqueBeanForBatchXMLEntry(bm, id);
        }

        if (match == null) {
            match = getUniqueBeanForClassName(bm, id);
        }

        return match;
    }

    /**
     * Use the given BeanManager to look up a unique CDI-registered bean
     * with bean name equal to 'batchId', using EL matching rules.
     *
     * @return the bean with the given bean name, or 'null' if there is an ambiguous resolution
     */
    protected Bean<?> getUniqueBeanByBeanName(BeanManager bm, String batchId) {
        Bean<?> match;

        // Get all beans with the given EL name (id).  EL names are applied via @Named.
        // If the bean is not annotated with @Named, then it does not have an EL name
        // and therefore can't be looked up that way.
        Set<Bean<?>> beans = bm.getBeans(batchId);

        try {
            match = bm.resolve(beans);
        } catch (AmbiguousResolutionException e) {
            return null;
        }
        return match;
    }

    /**
     * Use the given BeanManager to lookup a unique CDI-registered bean
     * with bean class equal to the batch.xml entry mapped to be the batchId parameter
     *
     * @return the bean with the given className. It returns null if there are zero matches or if there is no umabiguous resolution (i.e. more than 1 match)
     */
    protected Bean<?> getUniqueBeanForBatchXMLEntry(BeanManager bm, String batchId) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final ArtifactLocator artifactMap = createArtifactsLocator(tccl);
        final Class<?> clazz = artifactMap.getArtifactClassById(batchId);
        if (clazz != null) {
            try {
                return findUniqueBeanForClass(bm, clazz);
            } catch (AmbiguousResolutionException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    protected Bean<?> getUniqueBeanForClassName(BeanManager bm, String className) {
        // Ignore exceptions since will just failover to another loading mechanism
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return findUniqueBeanForClass(bm, clazz);
        } catch (AmbiguousResolutionException e) {
            return null;
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    /**
     * @return the bean within the given set whose class matches the given clazz.
     * @throws AmbiguousResolutionException if more than one match is found
     */
    protected Bean<?> findUniqueBeanForClass(BeanManager beanManager, Class<?> clazz) throws AmbiguousResolutionException {
        Set<Bean<?>> matches = new HashSet<Bean<?>>();
        Bean<?> retVal;
        Set<Bean<?>> beans = beanManager.getBeans(clazz);
        if (beans == null || beans.isEmpty()) {
            return null;
        }
        for (Bean<?> bean : beans) {
            if (bean.getBeanClass().equals(clazz)) {
                matches.add(bean);
            }
        }
        try {
            retVal = beanManager.resolve(matches);
        } catch (AmbiguousResolutionException e) {
            throw new AmbiguousResolutionException("Found beans = " + matches + ", and could not resolve unambiguously");
        }

        return retVal;
    }

    @Override
    public void init(final Properties batchConfig) throws BatchContainerServiceException {
        // no-op
    }

    protected BeanManager getBeanManager() {
        final BatchCDIInjectionExtension instance = BatchCDIInjectionExtension.getInstance();
        if (instance == null) {
            return null;
        }
        return instance.getBeanManager();
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
