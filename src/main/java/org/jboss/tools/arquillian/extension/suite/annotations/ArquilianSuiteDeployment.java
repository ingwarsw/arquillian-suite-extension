package org.jboss.tools.arquillian.extension.suite.annotations;

import org.jboss.arquillian.core.api.annotation.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate main class for Arquillian deployment.
 * 
 * @author Karol Lassak <ingwar@ingwar.eu.org>
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ArquilianSuiteDeployment {

}
