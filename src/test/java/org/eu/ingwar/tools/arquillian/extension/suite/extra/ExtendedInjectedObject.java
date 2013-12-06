package org.eu.ingwar.tools.arquillian.extension.suite.extra;
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

import org.eu.ingwar.tools.arquillian.extension.suite.inject.InjectedObject;

/**
 *
 * @author Karol Lassak 'Ingwar' <ingwar@ingwar.eu.org>
 */
public class ExtendedInjectedObject implements InjectedObject {
    public static final String NAME = "extra";
    
    public String getName() {
        return NAME;
    }
    
}
