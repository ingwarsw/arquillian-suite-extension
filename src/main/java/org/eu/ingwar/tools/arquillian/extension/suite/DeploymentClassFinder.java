package org.eu.ingwar.tools.arquillian.extension.suite;

/*
 * #%L
 * Arquillian suite extension
 * %%
 * Copyright (C) 2013 Ingwar & co.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang.StringUtils;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class DeploymentClassFinder {
    private static final Logger log = Logger.getLogger(DeploymentClassFinder.class.getName());

    /**
     * Finds class to be used as global deployment.
     *
     * @param descriptor ArquillianDescriptor
     * @return class to be used as global deployment.
     */
    static Class<?> getDeploymentClass(ArquillianDescriptor descriptor) {
        Class<?> deploymentClass = getDeploymentClassFromConfig(descriptor);
        if (deploymentClass == null) {
            deploymentClass = getDeploymentClassFromAnnotation(descriptor);
        }
        if (deploymentClass == null) {
            log.warning("arquillian-suite-deployment: Cannot find configuration in arquillian.xml, nor class annotated with @ArquillianSuiteDeployment, will try standard way..");
        }
        return deploymentClass;
    }

    /**
     * Finds class with should produce global deployment PER project.
     * 
     * @param descriptor ArquillianDescriptor
     * @return class marked witch @ArquillianSuiteDeployment annotation
     */
    private static Class<?> getDeploymentClassFromAnnotation(ArquillianDescriptor descriptor) {
    	final Reflections reflections;
    	if (shouldScanFullClasspath(descriptor)) {
    		// Clients can opt-in to search on the full classpath.
    		// In some setups (such as when running tests through maven surefire plugin)
    		// the classloader doesn't give access to the full classpath.
			// When the classpath is loaded though a "booter jar" (which contain the actual
			// classpath as manifest attribute) we can still access the fully loaded java
			// classpath dynamically, that we give as second argument here.
    		// Note that this may not be sufficient in some setups ; this is a best effort.
    		reflections = new Reflections(ClasspathHelper.contextClassLoader(), ClasspathHelper.forJavaClassPath());
    	} else {
		    // Had a bug that if you open inside eclipse more than one project with @ArquillianSuiteDeployment and is a dependency, the test doesn't run because found more than one @ArquillianSuiteDeployment.
		    // Filter the deployment PER project.
			reflections = new Reflections(ClasspathHelper.contextClassLoader().getResource(""));
    	}
        Set<Class<?>> results = reflections.getTypesAnnotatedWith(ArquillianSuiteDeployment.class, true);
        if (results.isEmpty()) {
            return null;
        }
        // Verify if has more than one @ArquillianSuiteDeployment. We cannot decide in cases we find more than one class.
        // in that case we will return null and hope there is a configuration in arquillian xml.
        if (results.size() > 1) {
            for (final Class<?> type : results) {
                log.log(Level.SEVERE, "arquillian-suite-deployment: Duplicated class annotated with @ArquillianSuiteDeployment: {0}", type.getName());
            }
            throw new IllegalStateException("Duplicated classes annotated with @ArquillianSuiteDeployment");
        }
        // Return the single result.
        final Class<?> type = results.iterator().next();
        log.log(Level.INFO,"arquillian-suite-deployment: Found class annotated with @ArquillianSuiteDeployment: {0}", type.getName());
        return type;
    }
    
    /**
     * Indicates if the deployment annotated class should be searched across the whole available classpath.
     * Extension name used: suite
     * Key: fullClasspathScan
     *
     * @param descriptor ArquillianDescriptor
     * @return flag value from arquillian.xml
     */
    private static boolean shouldScanFullClasspath(ArquillianDescriptor descriptor) {
        ExtensionDef extension = descriptor.extension("suite");
        if (extension != null) {
            String fullClasspathScan = extension.getExtensionProperties().get("fullClasspathScan");
            if (Boolean.valueOf(fullClasspathScan)) {
            	return true;
            }
        }
        return false;
    }

    /**
     * Finds class with should produce global deployment PER project.
     *
     * @param descriptor ArquillianDescriptor
     * @return class defined in arquillian.xml as deploymentClass
     */
    private static Class<?> getDeploymentClassFromConfig(ArquillianDescriptor descriptor) {
        String deploymentClassName = getDeploymentClassNameFromXml(descriptor);

        if (StringUtils.isNotEmpty(deploymentClassName)) {
            try {
                log.log(Level.INFO, "arquillian-suite-deployment: Using deployment class {0} from configuration.", deploymentClassName);
                return Class.forName(deploymentClassName);

            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load class " + deploymentClassName + " from configuration.");
            }
        }
        return null;
    }

    /**
     * Returns name of class witch should be used as global deployment.
     * Extension name used: suite
     * Key: deploymentClass
     *
     * @param descriptor ArquillianDescriptor
     * @return full class name from arquillian.xml
     */
    private static String getDeploymentClassNameFromXml(ArquillianDescriptor descriptor) {
        ExtensionDef extension = descriptor.extension("suite");
        if (extension != null) {
            return extension.getExtensionProperties().get("deploymentClass");
        }
        return null;
    }

}
