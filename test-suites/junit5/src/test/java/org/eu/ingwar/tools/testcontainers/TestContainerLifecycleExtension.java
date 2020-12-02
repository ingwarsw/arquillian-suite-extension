package org.eu.ingwar.tools.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 *
 * @author lprimak
 */
public class TestContainerLifecycleExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static GenericContainer<?> payara;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (payara == null) {
            payara = new FixedPortContainer<>(DockerImageName.parse("payara/server-full"))
                    .withFixedExposedPort(4848, 4848)
                    .withFixedExposedPort(8080, 8080)
                    .waitingFor(Wait.forHttp("/").forPort(8080));
            payara.start();
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("TestContaiersContext", this);
        }
    }

    @Override
    public void close() throws Throwable {
        payara.stop();
        payara = null;
    }
}
