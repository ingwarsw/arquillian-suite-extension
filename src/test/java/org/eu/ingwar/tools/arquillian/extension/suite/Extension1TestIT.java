package org.eu.ingwar.tools.arquillian.extension.suite;

import javax.enterprise.inject.spi.BeanManager;


import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class Extension1TestIT {

    @Test
    public void shouldInject(BeanManager bm) {
        System.out.println("IT Test1");
        Assert.assertNotNull(bm);
    }
}
