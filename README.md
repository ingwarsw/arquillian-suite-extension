The Arquillian Suite Extension
==================================

[![Build Status](https://travis-ci.org/ingwarsw/arquillian-suite-extension.png)](https://travis-ci.org/ingwarsw/arquillian-suite-extension)
[![Coverage Status](https://coveralls.io/repos/ingwarsw/arquillian-suite-extension/badge.png?branch=master)](https://coveralls.io/r/ingwarsw/arquillian-suite-extension?branch=master)

The Extension will force all Classes in a Module into a TestSuite running from the same DeploymentScenario.

Class marked with @ArquilianSuiteDeployment will be used as a 'template' for all other TestClass scenarios.

Deploy will occur only once on first test witch require Arquillian.

So far tested on:
- Jboss 7.1, Jboss 7.2
- Glassfish 3.2.2
- Should work on other servers too

### Usage

Add module to test classpath.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-suite-extension</artifactId>
        <version>1.0.3</version>
        <scope>test</scope>
    </dependency>

Mark one of your test classes with annotation @ArquilianSuiteDeployment along with usual @Deployment annotation on method.

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

### Credits

Most work was done by Aslak Knutsen
- https://gist.github.com/aslakknutsen/3975179
- Part was added by ItCrowd team.

I just mixed things up..

### Continuous integration

Travis CI builds the plugin with Oracle and Open JDK 7. All successfully built snapshots are deployed to
Sonatype OSS repository. Jacoco is used to gather coverage metrics and the report is submitted
to Coveralls with this plugin.


### License

The project is licensed under the Apache License, Version 2.0.
