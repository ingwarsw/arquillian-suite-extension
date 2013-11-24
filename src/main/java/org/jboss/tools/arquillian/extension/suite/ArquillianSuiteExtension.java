package org.jboss.tools.arquillian.extension.suite;

import org.jboss.tools.arquillian.extension.suite.annotations.ExtendedSuiteScoped;
import org.jboss.tools.arquillian.extension.suite.annotations.ArquilianSuiteDeployment;
import java.util.Set;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;

import java.util.concurrent.Callable;
import org.reflections.Reflections;

/**
 * Arquillian Suite Extension main class.
 *
 * @author Karol Lassak <ingwar@ingwar.eu.org>
 */
public class ArquillianSuiteExtension implements LoadableExtension {

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(SuiteDeployer.class).context(ExtendedSuiteContextImpl.class);
    }

    /**
     *
     */
    public static class SuiteDeployer {

        @Inject // Active some form of ClassContext around our deployments due to assumption bug in AS7 extension.
        private Instance<ClassContext> classContext;
        @Inject
        @ClassScoped
        private InstanceProducer<DeploymentScenario> classDeploymentScenario;
        private Class<?> deploymentClass;
        @Inject
        private Event<DeploymentEvent> deploymentEvent;
        @Inject
        private Instance<ExtendedSuiteContext> extendedSuiteContext;
        @Inject
        private Event<GenerateDeployment> generateDeploymentEvent;
        private boolean suiteDeploymentGenerated;
        private DeploymentScenario suiteDeploymentScenario;
        @ExtendedSuiteScoped
        @Inject
        private InstanceProducer<DeploymentScenario> suiteDeploymentScenarioInstanceProducer;

        /**
         * Method ignoring DeployManagedDeployments events.
         *
         * @param ignored Event to ignore
         */
        public void blockDeployManagedDeployments(@Observes EventContext<DeployManagedDeployments> ignored) {
            // Do nothing with event.
        }

        /**
         * Method ignoring GenerateDeployment events if deployment is already
         * done.
         *
         * @param eventContext Event to ignore or fire.
         */
        public void blockSubsquentGenerateDeployment(@Observes EventContext<GenerateDeployment> eventContext) {
            if (suiteDeploymentGenerated) {
                // Do nothing with event.
                return;
            }
            eventContext.proceed();
            suiteDeploymentGenerated = true;
        }

        /**
         * Method ignoring UnDeployManagedDeployments events.
         *
         * @param ignored Event to ignore
         */
        public void blockUnDeployManagedDeployments(@Observes EventContext<UnDeployManagedDeployments> ignored) {
            // Do nothing with event.
        }

        /**
         * Deploy event.
         * 
         * @param event event to observe
         * @param registry ContainerRegistry
         */
        public void deploy(@Observes final AfterStart event, final ContainerRegistry registry) {
            executeInClassScope(new Callable<Void>() {
                @Override
                public Void call() {
                    for (Deployment d : suiteDeploymentScenario.deployments()) {
                        deploymentEvent.fire(new DeployDeployment(findContainer(registry, event.getDeployableContainer()), d));
                    }
                    final ExtendedSuiteContext extendedSuiteContextLocal = SuiteDeployer.this.extendedSuiteContext.get();
                    if (!extendedSuiteContextLocal.isActive()) {
                        extendedSuiteContextLocal.deactivate();
                    }
                    return null;
                }
            });
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
         * Iterate in Registry to find ours container.
         * 
         * @param registry registy to find container in
         * @param deployable container to find
         * @return found container
         */
        private Container findContainer(ContainerRegistry registry, DeployableContainer<?> deployable) {
            for (Container container : registry.getContainers()) {
                if (container.getDeployableContainer() == deployable) {
                    return container;
                }
            }
            return null;
        }

        /**
         * Finds class with should produce global deployment for project.
         * 
         * @return class marked witch @ArquilianSuiteDeployment annotation
         */
        private Class<?> getDeploymentClass() {
            Reflections reflections = new Reflections("");
            Set<Class<?>> results = reflections.getTypesAnnotatedWith(ArquilianSuiteDeployment.class, false);

            if (results.isEmpty()) {
                throw new IllegalArgumentException("Cannot find class annotated with @ArquilianSuiteDeployment");
            }
            if (results.size() > 1) {
                for (Class<?> type : results) {
                    System.err.println("Duplicated class: " + type.getName()); // NOPMD
                }
                throw new IllegalArgumentException("Duplicated classess annotated with @ArquilianSuiteDeployment");
            }
            return results.iterator().next();
        }

        /**
         * Startup event.
         * 
         * @param event event to observe
         * @param descriptor ArquillianDescriptor
         */
        public void startup(@Observes(precedence = -100) ManagerStarted event, ArquillianDescriptor descriptor) {
            deploymentClass = getDeploymentClass();

            executeInClassScope(new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        generateDeploymentEvent.fire(new GenerateDeployment(new TestClass(deploymentClass)));
                        suiteDeploymentScenario = classDeploymentScenario.get();
                    } catch (Exception ex) {
                        ex.printStackTrace(); // NOPMD
                    }
                    return null;
                }
            });
            extendedSuiteContext.get().activate();
            suiteDeploymentScenarioInstanceProducer.set(suiteDeploymentScenario);
        }

        /**
         * Undeploy event.
         * 
         * @param event event to observe
         * @param registry ContainerRegistry
         */
        public void undeploy(@Observes final BeforeStop event, final ContainerRegistry registry) {
            executeInClassScope(new Callable<Void>() {
                @Override
                public Void call() {
                    for (Deployment d : suiteDeploymentScenario.deployments()) {
                        deploymentEvent.fire(new UnDeployDeployment(findContainer(registry, event.getDeployableContainer()), d));
                    }
                    return null;
                }
            });
        }
    }
}
