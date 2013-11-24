package org.jboss.tools.arquillian.extension.suite;

import org.jboss.tools.arquillian.extension.suite.annotations.ArquilianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

@ArquilianSuiteDeployment
public class Deployments {

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(Extension1Test.class.getPackage());
    }
}
