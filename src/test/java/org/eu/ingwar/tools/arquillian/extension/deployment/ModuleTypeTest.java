/*
 * Copyright 2013 Ingwar & co..
 *
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
 */
package org.eu.ingwar.tools.arquillian.extension.deployment;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Karol Lassak 'Ingwar' <ingwar@ingwar.eu.org>
 */
public class ModuleTypeTest {

    /**
     * Test of valueOf method, of class ModuleType.
     */
    @Test
    public void testValueOf() {
        String name = "EJB";
        ModuleType expResult = ModuleType.EJB;
        ModuleType result = ModuleType.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class ModuleType.
     */
    @Test
    public void testToString() {
        ModuleType instance = ModuleType.WAR;
        String expResult = "war";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class ModuleType.
     */
    @Test
    public void testGetType() {
        testGetTypeOne(ModuleType.JAR, JavaArchive.class);
        testGetTypeOne(ModuleType.EJB, JavaArchive.class);
        testGetTypeOne(ModuleType.WAR, WebArchive.class);
    }
    
    private void testGetTypeOne(ModuleType type, Class<? extends Archive<?>>  expected) {
        Class<? extends Archive<?>>  result = type.getType();
        assertEquals(expected, result);
    }

    /**
     * Test of isModule method, of class ModuleType.
     */
    @Test
    public void testIsModule() {
        testIsModuleOne(ModuleType.JAR, false);
        testIsModuleOne(ModuleType.EJB, true);
        testIsModuleOne(ModuleType.WAR, true);
    }
    
    private void testIsModuleOne(ModuleType type, boolean expected) {
        boolean result = type.isModule();
        assertEquals(expected, result);
    }

    /**
     * Test of getExtension method, of class ModuleType.
     */
    @Test
    public void testGetExtension() {
        testGetExtensionOne(ModuleType.JAR, "jar");
        testGetExtensionOne(ModuleType.EJB, "jar");
        testGetExtensionOne(ModuleType.WAR, "war");
    }
    
    private void testGetExtensionOne(ModuleType type, String expected) {
        String result = type.getExtension();
        assertEquals(expected, result);        
    }

    /**
     * Test of fromString method, of class ModuleType.
     */
    @Test
    public void testFromString() {
        testFromStringOne(ModuleType.JAR, "jar");
        testFromStringOne(ModuleType.EJB, "ejb");
        testFromStringOne(ModuleType.WAR, "war");
    }
    
    private void testFromStringOne(ModuleType expected, String type) {
        ModuleType result = ModuleType.fromString(type);
        assertEquals(expected, result);        
    }

    /**
     * Test of generateModuleName method, of class ModuleType.
     */
    @Test
    public void testGenerateModuleName() {
        ModuleType instance = ModuleType.JAR;
        String result = instance.generateModuleName();
        assertNotNull("Ensure module name is not null", result);
    }
}