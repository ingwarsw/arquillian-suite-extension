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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ArchiveImportException;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.filter.ExcludeRegExpPaths;
import org.jboss.shrinkwrap.impl.base.filter.IncludeRegExpPaths;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.jboss.shrinkwrap.resolver.api.InvalidConfigurationFileException;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenArtifactInfo;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy;

/**
 *
 * @author Karol Lassak 'Ingwar'
 */
public class EarGenericBuilder {

    private static final String RUN_AT_ARQUILLIAN_PATH = "/runs-at-arquillian.txt";
    private static final String RUN_AT_ARQUILLIAN_CONTENT = "at-arquillian";
    private static final Logger LOG = Logger.getLogger(EarGenericBuilder.class.getName());

    /**
     * Private constructor.
     */
    private EarGenericBuilder() {
    }

    /**
     * Generates deployment for given application.
     *
     * @param type Module type to generate
     * @return EnterpriseArchive containing given module and all dependencies
     */
    public static EnterpriseArchive getModuleDeployment(ModuleType type) {
        return getModuleDeployment(type, type.generateModuleName());
    }

    /**
     * Generates deployment for given application.
     *
     * @param type Module type to generate
     * @param basename Base name of module to generate
     * @return EnterpriseArchive containing given module and all dependencies
     */
    public static EnterpriseArchive getModuleDeployment(ModuleType type, String basename) {
        return getModuleDeployment(type, basename, true);
    }

    /**
     * Generates deployment for given application.
     *
     * @param type Module type to generate
     * @param basename Base name of module to generate
     * @param doFiltering should do basic filtering
     * @return EnterpriseArchive containing given module and all dependencies
     */
    public static EnterpriseArchive getModuleDeployment(ModuleType type, String basename, boolean doFiltering) {
        String name = basename + "." + type.getExtension();
        String testJarName = basename + "-tests.jar";
//        LOG.debug("Creating Arquillian deployment for [" + name + "]");
        try {
            EarDescriptorBuilder descriptorBuilder = new EarDescriptorBuilder(basename);
            MavenResolverSystem maven = Maven.resolver();
            //ConfigurableMavenResolverSystem maven = Maven.configureResolver().workOffline().withMavenCentralRepo(false);
            EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, basename + "-full.ear");
            PomEquippedResolveStage resolveStage = maven.loadPomFromFile("pom.xml");

            // przejrzenie dependency oznaczonych jako provided w celu znalezienia EJB'ków
            MavenResolvedArtifact[] provided = resolveStage.importRuntimeDependencies().importDependencies(ScopeType.PROVIDED).resolve().using(new AcceptScopesStrategy(ScopeType.PROVIDED)).asResolvedArtifact();
            for (MavenResolvedArtifact mra : provided) {
//                System.out.println("Checking provided: " + mra.getCoordinate().toCanonicalForm());
                if (isArtifactEjb(mra.getCoordinate())) {
                    ear.addAsModule(mra.as(JavaArchive.class));
                    // dodajemy jako moduł
                    descriptorBuilder.addEjb(mra.asFile().getName());
                    // przeglądamy dependency EJB'ka w celu pobrania także zależności z EJB'ka
                    for (MavenArtifactInfo mai : mra.getDependencies()) {
//                            LOG.debug("Resolved: " + mai.getCoordinate().getGroupId() + ":" + mai.getCoordinate().getArtifactId());
                        // pomijamy wzajemne zależności do innych EJB'ków
                        if (!isArtifactEjb(mai.getCoordinate())) {
                            for (MavenResolvedArtifact reqMra : provided) {
                                if (reqMra.getCoordinate().toCanonicalForm().equals(mai.getCoordinate().toCanonicalForm())) {
                                    // dodanie zależności do lib'ów
                                    ear.addAsLibrary(reqMra.asFile());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            MavenResolvedArtifact[] deps = resolveStage.importRuntimeAndTestDependencies().resolve().withTransitivity().asResolvedArtifact();

            for (MavenResolvedArtifact mra : deps) {
                MavenCoordinate mc = mra.getCoordinate();
                PackagingType packaging = mc.getPackaging();
                if (doFiltering && isFiltered(mc)) {
                    continue;
                }
                LOG.log(Level.FINEST, "Adding: {0}", mc.toCanonicalForm());
                if (isArtifactEjb(mc)) {
                    // dependency w postaci ejb'ków
                    ear.addAsModule(mra.as(JavaArchive.class));
                    descriptorBuilder.addEjb(mra.asFile().getName());
                } else if (packaging.equals(PackagingType.WAR)) {
                    // dependency w postaci war'ów
                    ear.addAsModule(mra.as(WebArchive.class));
                    descriptorBuilder.addWeb(mra.asFile().getName());
                } else {
                    // resztę dodajemy jako lib
                    ear.addAsLibrary(mra.asFile());
                }
            }
            // utworzenie głównego archiwum
//            Archive<?> module = ShrinkWrap.create(MavenImporter.class, name)
//                    .loadPomFromFile("pom.xml")
//                    .as(type.getType());

            Archive<?> module = ShrinkWrap.create(ExplodedImporter.class, name)
                    .importDirectory(type.getExplodedDir(basename))
                    .as(type.getType());

            JavaArchive testJar = ShrinkWrap.create(ExplodedImporter.class, testJarName)
                    .importDirectory("target/test-classes")
                    .as(JavaArchive.class);

            module = module.merge(testJar, type.getMergePoint());
//            mergeReplace(ear, module, testJar);

            module.add(new StringAsset(RUN_AT_ARQUILLIAN_CONTENT), RUN_AT_ARQUILLIAN_PATH);
            LOG.log(Level.FINE, module.toString(true));

            addMainModule(ear, type, module, descriptorBuilder);
            
            // Workaround for arquillian bug
            if (!descriptorBuilder.containsWar()) {
                String testModuleName = ModuleType.WAR.generateModuleName() + ".war";
                ear.addAsModule(ShrinkWrap.create(WebArchive.class, testModuleName));
                descriptorBuilder.addWeb(testModuleName);
            }

            ear.setApplicationXML(new StringAsset(descriptorBuilder.render()));
            ear.addManifest();
            LOG.log(Level.INFO, "Created deployment [{0}]", ear.getName());
//            System.out.println(ear.toString(true));
//            System.out.println(descriptorBuilder.render());
            return ear;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Error in creating deployment [" + ex + "]", ex);
        } catch (InvalidConfigurationFileException ex) {
            throw new IllegalStateException("Error in creating deployment [" + ex + "]", ex);
        } catch (ArchiveImportException ex) {
            throw new IllegalStateException("Error in creating deployment [" + ex + "]", ex);
        }
    }

    /**
     * Sprawdza czy dany artefakt jest ejb (ale nie jest ejb-clientem). Artefakt w formie MavenCoordinate.
     *
     * FIXME: Z powodu bledu shrinkwrapa szukamy tak na lewo po nazwie.
     *
     * @param artifactCoordinate MavenCoordinate artefaktu do sprawdzenia
     * @return true jesli jest projektem ejb
     */
    private static boolean isArtifactEjb(MavenCoordinate artifactCoordinate) {
        if ("client".equals(artifactCoordinate.getClassifier())) {
            return false;
        }
        if (!artifactCoordinate.getGroupId().startsWith("pl.gov.coi")) {
            return false;
        }
        if (!artifactCoordinate.getArtifactId().toLowerCase().contains("ejb")) {
            return false;
        }
        return true;
    }

    /**
     * Dodaje główny moduł do archiwum EAR.
     *
     * @param ear archiwum EAR
     * @param type typ głównego modułu
     * @param module główny moduł
     * @param descriptorBuilder deskreptor builder dla EAR'a
     */
    private static void addMainModule(EnterpriseArchive ear, ModuleType type, Archive<?> module, EarDescriptorBuilder descriptorBuilder) {
        if (type.isModule()) {
            ear.addAsModule(module);
            if (type == ModuleType.EJB) {
                descriptorBuilder.addEjb(module.getName());
            }
            if (type == ModuleType.WAR) {
                descriptorBuilder.addWeb(module.getName());
            }
        } else {
            ear.addAsLibrary(module);
        }
    }

//    /**
//     * Merges additional (usualy test) archive into main module.
//     *
//     * @param module main module
//     * @param additional module
//     */
//    private static void mergeReplace(EnterpriseArchive ear, Archive<?> module, Archive<?> additional) {
//        for (Map.Entry<ArchivePath, Node> entry : additional.getContent().entrySet()) {
//            ArchivePath ap = new BasicPath(entry.getKey().get());
//            if (module.contains(ap) && module.get(ap).getAsset() != null) {
//                module.delete(ap);
//            }
//            Asset asset = entry.getValue().getAsset();
//            if (asset == null) {
//                module.addAsDirectory(ap);
//            } else if ("/META-INF/jboss-deployment-structure.xml".equals(ap.get())) {
//                ear.add(asset, ap);
//            } else {
//                module.add(asset, ap);
//            }
//        }
//    }

    /**
     * Check if artefact should be filtered (omitted from packaging).
     * <p>
     * By default all artefact witch groups start with org.jboss.(shrinkwrap|arqrquillian) are filtered.
     * </p>
     * @param artifactCoordinate Artifact coordinate to check
     * @return true if artifact should be filtered
     */
    private static boolean isFiltered(MavenCoordinate artifactCoordinate) {
        if (artifactCoordinate.getGroupId().startsWith("org.jboss.shrinkwrap")) {
            return true;
        }
        if (artifactCoordinate.getGroupId().startsWith("org.jboss.arquillian")) {
            return true;
        }
        if (artifactCoordinate.getGroupId().startsWith("org.jboss.as")) {
            return true;
        }
        return false;
    }
}
