package org.eu.ingwar.tools.arqsuite;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lprimak
 */
class NumberOfDeploymentsTest {
    @AfterAll
    static void checkDeployments() {
        assertEquals(1, Deployments.numOfDeployments, "Should only be one deployment");
    }

    @Test
    void dummy() {

    }
}
