package org.jboss.arquillian.extension.suite;

import org.jboss.arquillian.core.api.annotation.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtendedSuiteScoped {

}
