The Arquillian Suite Extension
==================================

[![Build Status](https://travis-ci.org/ingwarsw/arquillian-suite-extension.png)](https://travis-ci.org/ingwarsw/arquillian-suite-extension)


The Extension will force all Classes in a Module into a TestSuite running from the same DeploymentScenario.

The deploymentClass defined in in the "suite" extension configuration will be used as a 'template' for all other TestClass scenarios.

Usage
-----

Add module to classpath.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-suite-extension</artifactId>
    </dependency>

Mark one of your test classes with annotation @ArquilianSuiteDeployment.

@ArquilianSuiteDeployment
public class Deployments {

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(Deployments.class.getPackage());
    }
}

Run test either from IDE or maven.

**Make sure you use servlet protocol!** To do that add following to arquillian.xml:

    <defaultProtocol type="Servlet 3.0"/>

Credits
-------

Most work was done by Aslak Knutsen
- https://gist.github.com/aslakknutsen/3975179
- Part was added by ItCrowd team.
