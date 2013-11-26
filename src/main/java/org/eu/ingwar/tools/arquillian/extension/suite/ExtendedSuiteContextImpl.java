package org.eu.ingwar.tools.arquillian.extension.suite;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ExtendedSuiteScoped;
import org.jboss.arquillian.core.spi.HashObjectStore;
import org.jboss.arquillian.core.spi.context.AbstractContext;
import org.jboss.arquillian.core.spi.context.ObjectStore;

import java.lang.annotation.Annotation;

/**
 * Implementation of ExtendedScopeContext.
 *
 * @author Karol Lassak <ingwar@ingwar.eu.org>
 */
public class ExtendedSuiteContextImpl extends AbstractContext<String> implements ExtendedSuiteContext {

    private static final String SUITE_CONTEXT_ID = "extendedSuite";

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ExtendedSuiteScoped.class;
    }

    /**
     * There can only one Suite active, so we hard code the id to "Suite".
     * 
     * {@inheritDoc}
     */
    @Override
    public void activate() {
        super.activate(SUITE_CONTEXT_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy(SUITE_CONTEXT_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ObjectStore createNewObjectStore() {
        return new HashObjectStore();
    }
}
