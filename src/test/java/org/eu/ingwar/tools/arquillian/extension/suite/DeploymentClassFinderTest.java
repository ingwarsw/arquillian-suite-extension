package org.eu.ingwar.tools.arquillian.extension.suite;

import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.DefaultProtocolDef;
import org.jboss.arquillian.config.descriptor.api.EngineDef;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.shrinkwrap.descriptor.api.DescriptorExportException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests different ways of loading the deployment class using
 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}.
 * 
 * @author Stephane Wasserhardt
 */
public class DeploymentClassFinderTest {

	/**
	 * Contructor.
	 */
	public DeploymentClassFinderTest() {
	}

	/**
	 * Makes the current context class loader not able to find the deployment class,
	 * while running the provided test runnable.
	 * 
	 * @param test Test {@link Runnable} to run.
	 */
	private void runUsingMockedContextClassLoader(Runnable test) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		// Simulate the use of a classloader that don't reference the class annotated
		// with @ArquillianSuiteDeployment.
		try {
			Thread.currentThread().setContextClassLoader(new TestClassLoader(contextClassLoader));

			test.run();

		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * The {@link Deployments} class is annotated with
	 * {@link ArquillianSuiteExtension}, this test asserts that it is found by the
	 * default implementation of
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}.
	 */
	@Test
	public void testCanFindDeploymentUsingContextClassloader() {
		final ArquillianDescriptor descriptorMock = new ArquillianDescriptorMock();

		Class<?> deploymentClass = DeploymentClassFinder.getDeploymentClass(descriptorMock);
		Assert.assertEquals(Deployments.class, deploymentClass);
	}

	/**
	 * On the contrary to {@link #testCanFindDeploymentUsingContextClassloader()},
	 * if we use a Classloader that can't load the {@link Deployments} class, this
	 * test asserts that the default implementation of
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)} will
	 * not be able to find the class.
	 */
	@Test
	public void testCannotFindDeploymentUsingWrongClassloader() {
		final ArquillianDescriptor descriptorMock = new ArquillianDescriptorMock();

		Runnable test = new Runnable() {
			@Override
			public void run() {
				Class<?> deploymentClass = DeploymentClassFinder.getDeploymentClass(descriptorMock);
				Assert.assertNull(deploymentClass);
			}
		};

		runUsingMockedContextClassLoader(test);
	}

	/**
	 * As in {@link #testCannotFindDeploymentUsingWrongClassloader()}, the
	 * {@link Deployments} class will not be found by the default scanning
	 * implementation of
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}. This
	 * time, we also explicitly give the class name, so that we don't use scanning
	 * for the {@link ArquillianSuiteExtension} annotation. We test the
	 * {@link Deployments} class is then found by
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}.
	 */
	@Test
	public void testFindDeploymentFromConfigUsingExplicitClass() {
		final ArquillianDescriptor descriptorMock = new ArquillianDescriptorMock("deploymentClass",
				Deployments.class.getName());

		Runnable test = new Runnable() {
			@Override
			public void run() {
				Class<?> deploymentClass = DeploymentClassFinder.getDeploymentClass(descriptorMock);
				Assert.assertEquals(Deployments.class, deploymentClass);
			}
		};

		runUsingMockedContextClassLoader(test);
	}

	/**
	 * As in {@link #testCannotFindDeploymentUsingWrongClassloader()}, the
	 * {@link Deployments} class will not be found by the default scanning
	 * implementation of
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}. This
	 * time, we specify full classpath scanning. We test the {@link Deployments}
	 * class is then found by
	 * {@link DeploymentClassFinder#getDeploymentClass(ArquillianDescriptor)}.
	 */
	@Test
	public void testFindDeploymentFromAnnotationUsingFullClasspathScan() {
		final ArquillianDescriptor descriptorMock = new ArquillianDescriptorMock("fullClasspathScan",
				Boolean.TRUE.toString());

		Runnable test = new Runnable() {
			@Override
			public void run() {
				Class<?> deploymentClass = DeploymentClassFinder.getDeploymentClass(descriptorMock);
				Assert.assertEquals(Deployments.class, deploymentClass);
			}
		};

		runUsingMockedContextClassLoader(test);
	}

	/**
	 * A ClassLoader which is never able to load any class.
	 * 
	 * @author Stephane Wasserhardt
	 */
	private static class TestClassLoader extends URLClassLoader {
		public TestClassLoader(ClassLoader contextClassLoader) {
			super(((URLClassLoader) contextClassLoader).getURLs());
		}
		
		@Override
		public URL getResource(String name) {
			if ("".equals(name)) {
				// We end-up in this case when we do not use full classpath scanning.
				// We test the case where we obtain a valid classpath entry, but one which does not contain the annotated class.
				// This way, we must do a full classpath scan to find the annotated class.
				URL resource = super.getResource("META-INF");
				return resource;
			}
			return super.getResource(name);
		}
	}

	/**
	 * Mocks ArquillianDescriptor for {@link DeploymentClassFinderTest} use-cases.
	 * 
	 * @author Stephane Wasserhardt
	 */
	private static class ArquillianDescriptorMock implements ArquillianDescriptor {

		private final Map<String, String> props;

		public ArquillianDescriptorMock() {
			props = new HashMap<String, String>();
		}

		public ArquillianDescriptorMock(String key, String value) {
			props = new HashMap<String, String>();
			props.put(key, value);
		}

		@Override
		public String getDescriptorName() {
			return null;
		}

		@Override
		public String exportAsString() throws DescriptorExportException {
			return null;
		}

		@Override
		public void exportTo(OutputStream output) throws DescriptorExportException, IllegalArgumentException {
		}

		@Override
		public EngineDef engine() {
			return null;
		}

		@Override
		public DefaultProtocolDef defaultProtocol(String type) {
			return null;
		}

		@Override
		public DefaultProtocolDef getDefaultProtocol() {
			return null;
		}

		@Override
		public ContainerDef container(String name) {
			return null;
		}

		@Override
		public GroupDef group(String name) {
			return null;
		}

		@Override
		public ExtensionDef extension(String name) {
			if ("suite".equals(name)) {
				return new ExtensionDefMock();
			}
			return null;
		}

		@Override
		public List<ContainerDef> getContainers() {
			return null;
		}

		@Override
		public List<GroupDef> getGroups() {
			return null;
		}

		@Override
		public List<ExtensionDef> getExtensions() {
			return null;
		}

		private class ExtensionDefMock implements ExtensionDef {

			@Override
			public EngineDef engine() {
				return null;
			}

			@Override
			public DefaultProtocolDef defaultProtocol(String type) {
				return null;
			}

			@Override
			public DefaultProtocolDef getDefaultProtocol() {
				return null;
			}

			@Override
			public ContainerDef container(String name) {
				return null;
			}

			@Override
			public GroupDef group(String name) {
				return null;
			}

			@Override
			public ExtensionDef extension(String name) {
				return null;
			}

			@Override
			public List<ContainerDef> getContainers() {
				return null;
			}

			@Override
			public List<GroupDef> getGroups() {
				return null;
			}

			@Override
			public List<ExtensionDef> getExtensions() {
				return null;
			}

			@Override
			public String getDescriptorName() {
				return null;
			}

			@Override
			public String exportAsString() throws DescriptorExportException {
				return null;
			}

			@Override
			public void exportTo(OutputStream output) throws DescriptorExportException, IllegalArgumentException {
			}

			@Override
			public String getExtensionName() {
				return null;
			}

			@Override
			public ExtensionDef setExtensionName(String name) {
				return null;
			}

			@Override
			public ExtensionDef property(String name, String value) {
				return null;
			}

			@Override
			public Map<String, String> getExtensionProperties() {
				return props;
			}

			@Override
			public String getExtensionProperty(String name) {
				return props.get(name);
			}

		}

	}

}
