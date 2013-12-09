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

/**
 * Typ modułu archiwum.
 *
 * @author Karol Lassak 'Ingwar' <karol.lassak@coi.gov.pl>
 */
public enum ModuleType {

    WAR("war"),
    EJB("ejb"),
    JAR("jar");
    
    private String type;

    /**
     * Domyślny konstruktor.
     *
     * @param type typ modułu
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
    private static ModuleType fromString(String packaging) {
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
}
