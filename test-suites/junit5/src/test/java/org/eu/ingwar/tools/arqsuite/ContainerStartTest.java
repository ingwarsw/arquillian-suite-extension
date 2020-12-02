package org.eu.ingwar.tools.arqsuite;

import org.eu.ingwar.tools.testcontainers.TestContainerLifecycleExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author lprimak
 */
@Tag("TestContainers")
@ExtendWith(TestContainerLifecycleExtension.class)
public class ContainerStartTest {
    @Test
    void dummy() {
    }
}
