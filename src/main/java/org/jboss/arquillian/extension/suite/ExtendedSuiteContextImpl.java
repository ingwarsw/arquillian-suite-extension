package org.jboss.arquillian.extension.suite;

import org.jboss.arquillian.core.spi.HashObjectStore;
import org.jboss.arquillian.core.spi.context.AbstractContext;
import org.jboss.arquillian.core.spi.context.ObjectStore;

import java.lang.annotation.Annotation;

public class ExtendedSuiteContextImpl extends AbstractContext<String> implements ExtendedSuiteContext {

    private static final String SUITE_CONTEXT_ID = "extendedSuite";

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.Context#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope()
    {
        return ExtendedSuiteScoped.class;
    }

    /**
     * There can only one Suite active, so we hard code the id to "Suite".
     */
    @Override
    public void activate()
    {
        super.activate(SUITE_CONTEXT_ID);
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.impl.context.AbstractContext#destroy(java.lang.Object)
     */
    @Override
    public void destroy()
    {
        super.destroy(SUITE_CONTEXT_ID);
    }

    @Override
    protected ObjectStore createNewObjectStore()
    {
        return new HashObjectStore();
    }
}
