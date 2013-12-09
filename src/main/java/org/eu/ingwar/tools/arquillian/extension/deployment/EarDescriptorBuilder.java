package org.eu.ingwar.tools.arquillian.extension.deployment;

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

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builder do deskrptora Ear'a.
 */
public class EarDescriptorBuilder {

    private final String basename;

    private final List<String> ejbs = new ArrayList<>();

    private final List<Map.Entry<String, String>> webs = new ArrayList<>();

    private Document doc;

    private Element rootElement;

    private boolean addRandom = true;

    public void setAddRandom(boolean addRandom) {
        this.addRandom = addRandom;
    }

    /**
     * Konstruktor.
     *
     * @param basename bazowa nazwa dla Eara
     */
    public EarDescriptorBuilder(String basename) {
        this.basename = basename;
    }

    /**
     * Dodaje moduł EJB.
     *
     * @param filename nazwa
     * @return this
     */
    public EarDescriptorBuilder addEjb(String filename) {
        this.ejbs.add(filename);
        return this;
    }

    /**
     * Dodaje moduł webowy.
     *
     * @param filename nazwa
     * @param context kontekst
     * @return this
     */
    public EarDescriptorBuilder addWeb(String filename, String context) {
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(filename, context);
        this.webs.add(entry);
        return this;
    }

    /**
     * Dodaje moduł webowy.
     *
     * @param filename nazwa
     * @return this
     */
    public EarDescriptorBuilder addWeb(String filename) {
        String context;
        context = removeExtension(filename).replaceAll("[^a-z]", "");
        if (addRandom) {
            Random random = new Random();
            context += "-" + random.nextLong();
        }
        return addWeb(filename, context);
    }

    /**
     * Usuwa rozszerzenie z nazwy pliku.
     *
     * @param filename nazwa pliku
     * @return plik bez rozszerzenia
     */
    private String removeExtension(String filename) {
        return filename.replaceAll("\\.[a-z]{2,}$", "");
    }

    /**
     * Renderuje XML aplikacji.
     *
     * @return aplication xml
     */
    public String render() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            doc = docBuilder.newDocument();
            rootElement = doc.createElement("application");
            Element element;
            rootElement.setAttribute("xmlns", "http://java.sun.com/xml/ns/javaee");
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xsi:schemaLocation",
                    "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd");
            rootElement.setAttribute("version", "6");
            doc.appendChild(rootElement);

            element = doc.createElement("description");
            rootElement.appendChild(element);

            element = doc.createElement("display-name");
            element.setTextContent(basename + "-full");
            rootElement.appendChild(element);

            for (String filename : ejbs) {
                writeEjbModule(filename);
            }

            for (Map.Entry<String, String> entry : webs) {
                writeWebModule(entry.getKey(), entry.getValue());
            }

            element = doc.createElement("library-directory");
            element.setTextContent("lib");
            rootElement.appendChild(element);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(bytes);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);

            return bytes.toString();
        } catch (TransformerException | ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Zapis webowego modułu.
     *
     * @param filename nazwa pliku
     * @param context context
     */
    private void writeWebModule(String filename, String context) {
        Element element = writeModule();
        Element web = doc.createElement("web");
        element.appendChild(web);
        Element webUri = doc.createElement("web-uri");
        Element contextRoot = doc.createElement("context-root");
        web.appendChild(webUri);
        web.appendChild(contextRoot);
        webUri.setTextContent(filename);
        contextRoot.setTextContent("/" + context);
    }

    /**
     * Zapisuje moduł EJB.
     *
     * @param filename nazwa pliku
     */
    private void writeEjbModule(String filename) {
        Element element = writeModule();
        Element ejb = doc.createElement("ejb");
        element.appendChild(ejb);
        ejb.setTextContent(filename);
    }

    /**
     * Zapisuje moduł.
     *
     * @return element
     */
    private Element writeModule() {
        Element element = doc.createElement("module");
        rootElement.appendChild(element);
        return element;
    }
}