package org.eu.ingwar.tools.arquillian.extension.suite.annotations;

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

import org.jboss.arquillian.core.api.annotation.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate main class for Arquillian deployment.
 * 
 * In each project can be only one class marked witch that annotation
 * 
 * @author Karol Lassak 'Ingwar'
 * @deprecated 
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface ArquilianSuiteDeployment {

}
