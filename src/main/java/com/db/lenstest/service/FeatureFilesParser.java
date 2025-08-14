package com.db.lenstest.service;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.plugin.event.TestSourceRead;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FeatureFilesParser {
    private static final String FEATURE_FILES_PATH = "src/main/resources/features";

    @SneakyThrows
    public Set<String> getAllTags() {
        Set<String> tags = new TreeSet<>();

        GherkinParser parser = GherkinParser.builder().build();

        Files.walk(Paths.get(FEATURE_FILES_PATH))
                .filter(path -> path.toString().endsWith(".feature"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);

                        parser.parse(path.toString(), content.getBytes())
                                .forEach(envelope -> {
                                    envelope.getGherkinDocument()
                                            .flatMap(GherkinDocument::getFeature)
                                            .ifPresent(feature -> extractTags(feature, tags));
                                });

                    } catch (IOException e) {
                        throw new RuntimeException("Error reading feature file: " + path, e);
                    }
                });

        return tags;
    }

    private void extractTags(Feature feature, Set<String> tags) {
        // Feature-level tags
        feature.getTags().forEach(tag -> tags.add(tag.getName()));

        // Scenario-level tags
        feature.getChildren().forEach(child -> {
            if (child.getScenario().isPresent()) {
                Scenario scenario = child.getScenario().get();
                scenario.getTags().forEach(tag -> tags.add(tag.getName()));
            }
        });
    }

    public Optional<Feature> parseFeature(final TestSourceRead event) {
        final GherkinParser parser = GherkinParser.builder()
                .includePickles(false)
                .includeSource(false)
                .build();
        try {
            URI uri = Objects.requireNonNull(this.getClass().getClassLoader().getResource(event.getUri().getSchemeSpecificPart())).toURI();
            final Optional<Envelope> envelope = parser.parse(Paths.get(uri))
                    .findAny();
            if (envelope.isEmpty() || envelope.get().getGherkinDocument().isEmpty()) {
                log.error("No features were found in {}", event.getUri());
                return Optional.empty();
            }
            final GherkinDocument document = envelope.get().getGherkinDocument().get();
            if (document.getFeature().isEmpty()) {
                log.error("Feature file {} does not contain a Feature", event.getUri());
            }
            return document.getFeature();
        } catch (final IOException e) {
            log.error("Failed to load feature file {}", event.getUri(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
