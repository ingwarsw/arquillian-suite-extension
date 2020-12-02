package org.eu.ingwar.tools.arqsuite;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author lprimak
 */
@ArquillianSuiteDeployment
public class Deployments {
    public static int numOfDeployments;

    @Deployment
    public static WebArchive deploy() {
        ++numOfDeployments;
        return ShrinkWrap.create(WebArchive.class).addPackage(Deployments.class.getPackage());
    }
}
