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

import java.util.Random;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Type of generating arquillian module.
 *
 * @author Karol Lassak 'Ingwar'
 */
public enum ModuleType {

    /**
     * WAR archive.
     */
    WAR("war"),
    /**
     * EJB archive.
     */
    EJB("ejb"),
    /**
     * JAR archive.
     */
    JAR("jar");
    private String type;

    /**
     * Constructor.
     *
     * @param type module type
     */
    private ModuleType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    /**
     * Tworzy enum ze stringa.
     *
     * @param packaging nazwa typu pakowania
     * @return typ modułu
     */
    public static ModuleType fromString(String packaging) {
        for (ModuleType moduleType : ModuleType.values()) {
            if (moduleType.type.equals(packaging)) {
                return moduleType;
            }
        }
        throw new IllegalArgumentException("Nieprawidłowa wartość dla enuma: " + packaging);
    }

    /**
     * Pobiera typ archiwum.
     *
     * @return typ archiwum
     */
    public Class<? extends Archive<?>> getType() {
        switch (this) {
            case WAR:
                return WebArchive.class;
            case EJB:
            case JAR:
                return JavaArchive.class;
            default:
                throw new IllegalStateException("Illegal type for archive");
        }
    }

    /**
     * Sprawdza czy jest modułem czy bibioteką.
     *
     * @return true jeżeli jest modułem
     */
    public boolean isModule() {
        return (this != JAR);
    }

    /**
     * Pobiera rozszerzenie dla danego modułu.
     *
     * @return rozszerzenie
     */
    public String getExtension() {
        switch (this) {
            case WAR:
                return "war";
            case EJB:
            case JAR:
                return "jar";
            default:
                throw new IllegalStateException("Illegal type for extension");
        }
    }

    /**
     * Generates module name for given type.
     *
     * Adds random part to name.
     *
     * @return randomized module name
     */
    String generateModuleName() {
        String name;
        switch (this) {
            case WAR:
                name = "war";
                break;
            case EJB:
                name = "ejb";
                break;
            case JAR:
                name = "jar";
                break;
            default:
                throw new IllegalStateException("Illegal type for extension");
        }
        Random random = new Random();
        name += "_module_" + random.nextInt(Integer.MAX_VALUE);
        return name;
    }

    String getMergePoint() {
        if (this.equals(WAR)) {
            return "WEB-INF/classes";
        }
        return "";
    }

    String getExplodedDir(String basename) {
        if (this.equals(WAR)) {
            return "target/" + basename;
        }
        return "target/classes";
    }
}
