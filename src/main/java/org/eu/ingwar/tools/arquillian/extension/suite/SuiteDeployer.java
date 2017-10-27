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

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ExtendedSuiteScoped;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eu.ingwar.tools.arquillian.extension.suite.DeploymentClassFinder.getDeploymentClass;

/**
 *
 */
public class SuiteDeployer {
    private static final Logger log = Logger.getLogger(SuiteDeployer.class.getName());

    @Inject // Active some form of ClassContext around our deployments due to assumption bug in AS7 extension.
    private Instance<ClassContext> classContext;
    @Inject
    @ClassScoped
    private InstanceProducer<DeploymentScenario> classDeploymentScenario;
    @Inject
    private Event<UnDeployManagedDeployments> undeployEvent;
    @Inject
    private Event<GenerateDeployment> generateDeploymentEvent;
    @Inject
    private Instance<ExtendedSuiteContext> extendedSuiteContext;
    private DeploymentScenario suiteDeploymentScenario;
    @ExtendedSuiteScoped
    @Inject
    private InstanceProducer<DeploymentScenario> suiteDeploymentScenarioInstanceProducer;

    private boolean suiteDeploymentGenerated;
    private boolean deployDeployments;
    private boolean undeployDeployments;
    static Class<?> deploymentClass;

    /**
     * Callback for getting notified with configuration data. Here we use the optionally configured deployment class.
     *
     * @param descriptor The arquillian configuration data.
     */
    public static void configure(@Observes ArquillianDescriptor descriptor) {
        deploymentClass = getDeploymentClass(descriptor);
    }

    /**
     * Method ignoring DeployManagedDeployments events if already deployed.
     *
     * @param eventContext Event to check
     */
    public void blockDeployManagedDeploymentsWhenNeeded(@Observes EventContext<DeployManagedDeployments> eventContext) {
        if (!extensionEnabled()) {
            eventContext.proceed();
        }
        else if (deployDeployments) {
            deployDeployments = false;
            debug("NOT Blocking DeployManagedDeployments event {0}", eventContext.getEvent().toString());
            eventContext.proceed();
        } else {
            // Do nothing with event.
            debug("Blocking DeployManagedDeployments event {0}", eventContext.getEvent().toString());
        }
    }

    /**
     * Method ignoring GenerateDeployment events if deployment is already done.
     *
     * @param eventContext Event to check
     */
    public void blockGenerateDeploymentWhenNeeded(@Observes EventContext<GenerateDeployment> eventContext) {
        if (!extensionEnabled()) {
            eventContext.proceed();
        }
        else if (suiteDeploymentGenerated) {
            // Do nothing with event.
            debug("Blocking GenerateDeployment event {0}", eventContext.getEvent().toString());
        } else {
            suiteDeploymentGenerated = true;
            debug("NOT Blocking GenerateDeployment event {0}", eventContext.getEvent().toString());
            eventContext.proceed();
        }
    }

    /**
     * Method ignoring UnDeployManagedDeployments events at runtime.
     *
     * Only at undeploy container we will undeploy all.
     *
     * @param eventContext Event to check
     */
    public void blockUnDeployManagedDeploymentsWhenNeeded(@Observes EventContext<UnDeployManagedDeployments> eventContext) {
        if (!extensionEnabled()) {
            eventContext.proceed();
        }
        else if (undeployDeployments) {
            undeployDeployments = false;
            debug("NOT Blocking UnDeployManagedDeployments event {0}", eventContext.getEvent().toString());
            eventContext.proceed();
        } else {
            // Do nothing with event.
            debug("Blocking UnDeployManagedDeployments event {0}", eventContext.getEvent().toString());
        }
    }

    /**
     * Startup event.
     *
     * @param event AfterStart event to catch
     */
    public void startup(@Observes(precedence = -100) final BeforeSuite event) {
        if (extensionEnabled()) {
            debug("Catching BeforeSuite event {0}", event.toString());
            executeInClassScope(new Callable<Void>() {
                @Override
                public Void call() {
                    generateDeploymentEvent.fire(new GenerateDeployment(new TestClass(deploymentClass)));
                    suiteDeploymentScenario = classDeploymentScenario.get();
                    return null;
                }
            });
            deployDeployments = true;
            extendedSuiteContext.get().activate();
            suiteDeploymentScenarioInstanceProducer.set(suiteDeploymentScenario);
        }
    }

    /**
     * Undeploy event.
     *
     * @param event event to observe
     */
    public void undeploy(@Observes final BeforeStop event) {
        if (extensionEnabled()) {
            debug("Catching BeforeStop event {0}", event.toString());
            undeployDeployments = true;
            undeployEvent.fire(new UnDeployManagedDeployments());
        }
    }

    /**
     * Calls operation in deployment class scope.
     *
     * @param call Callable to call
     */
    private void executeInClassScope(Callable<Void> call) {
        try {
            classContext.get().activate(deploymentClass);
            call.call();
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke operation", e); // NOPMD
        } finally {
            classContext.get().deactivate();
        }
    }

    /**
     * Prints debug message.
     *
     * If arquillian.debug flag is set.
     *
     * @param format format of message
     * @param message message objects to format
     */
    private void debug(String format, Object... message) {
        Boolean DEBUG = Boolean.valueOf(System.getProperty("arquillian.debug"));
        if (DEBUG) {
            log.log(Level.WARNING, format, message);
        }
    }

    /**
     * Decides whenever extension is enabled.
     *
     * @return true if extension is enabled (found deployment class)
     */
    private boolean extensionEnabled() {
        return deploymentClass != null;
    }
}
