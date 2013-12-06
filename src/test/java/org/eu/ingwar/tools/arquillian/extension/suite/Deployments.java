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

import org.eu.ingwar.tools.arquillian.extension.suite.normal.Extension1Test;
import org.eu.ingwar.tools.arquillian.extension.suite.inject.InjectedObject;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquilianSuiteDeployment;
import org.eu.ingwar.tools.arquillian.extension.suite.extra.ExtensionExtra1Test;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

@ArquilianSuiteDeployment
public class Deployments {

    @Deployment(order = 1, name = "normal")
    public static WebArchive generateDefaultDeployment() {
        return ShrinkWrap.create(WebArchive.class, "normal.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(Deployments.class)
                .addClass(InjectedObject.class)
                .addPackage(Extension1Test.class.getPackage());
    }
    
    @Deployment(order = 2, name = "extra")
    public static Archive generateExtraDeployment() {
        Archive ejb = ShrinkWrap.create(JavaArchive.class, "extra_ejb.jar")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(Deployments.class)
                .addClass(InjectedObject.class)
                .addPackage(ExtensionExtra1Test.class.getPackage());
        return ShrinkWrap.create(EnterpriseArchive.class, "extra.ear").addAsLibraries(ejb);
    }
    
}
