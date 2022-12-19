package com.zebrunner.carina.utils.mobile;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.commons.artifact.ArtifactManagerFactory;
import com.zebrunner.carina.commons.artifact.IArtifactManager;
import com.zebrunner.carina.commons.artifact.IArtifactManagerFactory;

public final class ArtifactProvider implements IArtifactManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static IArtifactManager instance;
    private final List<IArtifactManagerFactory> artifactManagerFactories = new ArrayList<>();

    public ArtifactProvider() {
        Collection<URL> allPackagePrefixes = Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .map(s -> s.split("\\.")[0])
                .distinct()
                .map(ClasspathHelper::forPackage).reduce((c1, c2) -> {
                    Collection<URL> c3 = new HashSet<>();
                    c3.addAll(c1);
                    c3.addAll(c2);
                    return c3;
                }).orElseThrow();

        ConfigurationBuilder config = new ConfigurationBuilder().addUrls(allPackagePrefixes)
                .addScanners(new SubTypesScanner(false));

        Set<Class<?>> classes = new Reflections(config).getTypesAnnotatedWith(ArtifactManagerFactory.class);

        for (Class<?> clazz : classes) {
            if (!IArtifactManagerFactory.class.isAssignableFrom(clazz)) {
                LOGGER.error("Class {} marked by ArtifactManagerFactory annotation, but it is not realize interface IArtifactManagerFactory",
                        clazz.getName());
                continue;
            }
            try {
                IArtifactManagerFactory artifactManagerFactory = (IArtifactManagerFactory) ConstructorUtils.invokeConstructor(clazz);
                artifactManagerFactories.add(artifactManagerFactory);
            } catch (ClassCastException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                LOGGER.error("Cannot create instance of artifact factory class: {}", clazz.getName());
            }
        }
    }

    public static synchronized IArtifactManager getInstance() {
        if (instance == null) {
            instance = new ArtifactProvider();
        }
        return instance;
    }

    @Override
    public boolean download(String from, Path to) {
        IArtifactManagerFactory manager = artifactManagerFactories.stream()
                .filter(entity -> entity.isSuitable(from))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find suitable artifact manager for url: %s", from)));
        return manager.getInstance().download(from, to);
    }

    @Override
    public boolean put(Path from, String to) throws FileNotFoundException {
        IArtifactManagerFactory manager = artifactManagerFactories.stream()
                .filter(entity -> entity.isSuitable(to))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find suitable artifact manager for url: %s", to)));
        return manager.getInstance().put(from, to);
    }

    @Override
    public boolean delete(String url) {
        IArtifactManagerFactory manager = artifactManagerFactories.stream()
                .filter(entity -> entity.isSuitable(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find suitable artifact manager for url: %s", url)));
        return manager.getInstance().delete(url);
    }

    @Override
    public String getDirectLink(String url) {
        Optional<IArtifactManagerFactory> manager = artifactManagerFactories.stream()
                .filter(entity -> entity.isSuitable(url))
                .findFirst();
        if (manager.isEmpty()) {
            LOGGER.debug("Cannot find artifact manager to get direct link: '{}', so it will return as is", url);
        }

        return manager.isEmpty() ? url
                : manager.get()
                        .getInstance()
                        .getDirectLink(url);
    }
}
