package com.db.lenstest.listener;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.config.SpringContext;
import com.db.lenstest.lensDTO.*;
import com.db.lenstest.lensDTO.Step;
import com.db.lenstest.lensRepository.TestRunEntityRepository;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Tag;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CustomCucumberListener implements ConcurrentEventListener {

    TestRun testRun = new TestRun();
    ConcurrentSkipListSet<String> featureIds = new ConcurrentSkipListSet<>();

    private TestRunEntityRepository testRunEntityRepository = SpringContext.getBean(TestRunEntityRepository.class);


    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::runStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::runFinished);
        publisher.registerHandlerFor(TestSourceRead.class, this::featureRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::ScenarioStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::ScenarioFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::writeLogEvent);
    };

    private void runStarted(TestRunStarted event) {
        testRun.setExecutionStage(ExecutionStage.IN_PROGRESS);
        testRun.setFilterTag(System.getProperty("cucumber.filter.tags"));
        testRunEntityRepository
                .save(testRun.toEntity())
                .doOnSuccess(testRunEntity -> {
                    testRun.setId(testRunEntity.getId());
                })
                .subscribe();
    };

    private void runFinished(TestRunFinished event) {
        testRun.setCompletedAt(LocalDateTime.now());
        testRun.setExecutionStage(ExecutionStage.FINISHED);
        testRunEntityRepository
                .save(testRun.toEntity())
                .doOnSuccess(testRunEntity -> ResultPublisher.publish(testRunEntity))
                .subscribe();
    };

    private void featureRead(TestSourceRead event) {
        Optional<io.cucumber.messages.types.Feature> container = parseFeature(event);
        container.ifPresent(featureCucumber -> {
            final Stream<String> tags = featureCucumber.getTags().stream().map(Tag::getName);
            Feature feature = new Feature();
            feature.setId(event.getUri().getSchemeSpecificPart());
            feature.setName("Feature: "+featureCucumber.getName());
            feature.setDescription(featureCucumber.getDescription());
            feature.getTags().addAll(tags.collect(Collectors.toSet()));
            feature.setStatus(TestStatus.PASSED);

            testRun.addFeature(feature);
            testRun.getFeatureStats().getTotal().incrementAndGet();
            testRun.getFeatureStats().getPassed().incrementAndGet();
        });
    };

    private void ScenarioStarted(TestCaseStarted event) {
        log.debug("Scenario start: {}", event.getTestCase().getName());
        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();

        Scenario scenario = new Scenario();
        scenario.setId(scenarioId);
        scenario.setName(event.getTestCase().getKeyword() + ": " + event.getTestCase().getName());
        scenario.getTags().addAll(event.getTestCase().getTags());

        testRun.getFeatures().get(featureId).addScenario(scenario);
    };

    private void ScenarioFinished(TestCaseFinished event) {
        log.debug("Scenario end: {}", event.getTestCase().getName());

        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();

        Feature feature = testRun.getFeatures().get(featureId);
        Scenario scenario = feature.getScenarios().get(scenarioId);

        scenario.setCompletedAt(LocalDateTime.now());
        scenario.setStatus(TestStatus.parseStatus(event.getResult().getStatus().name()));
        testRun.updateScenarioStats(scenario.getStatus());

        TestStatus newFeatureStatus = TestStatus.computePriority(feature.getStatus(),scenario.getStatus());
        testRun.updateFeatureStatsAndStatus(feature,newFeatureStatus);
        feature.setCompletedAt(LocalDateTime.now());

        for(String tag: scenario.getTags()){
            testRun.updateTagStats(tag,scenario.getStatus());
        }

        testRunEntityRepository.save(testRun.toEntity())
                .doOnSuccess(ResultPublisher::publish)
                .subscribe();
    };

    private void stepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            final PickleStepTestStep pickle = ((PickleStepTestStep) event.getTestStep());
            log.debug("Step starting: {}", pickle.getStep().getText());

            String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
            UUID scenarioId = event.getTestCase().getId();
            UUID stepId = event.getTestStep().getId();

            Feature feature = testRun.getFeatures().get(featureId);
            Scenario scenario = feature.getScenarios().get(scenarioId);

            Step step = new Step();
            step.setId(stepId);
            step.setName(pickle.getStep().getKeyword() + pickle.getStep().getText());

            scenario.addStep(step);
        }
    };

    private void stepFinished(TestStepFinished event) {
        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();
        UUID stepId = event.getTestStep().getId();

        Feature feature = testRun.getFeatures().get(featureId);
        Scenario scenario = feature.getScenarios().get(scenarioId);

        Step step = null;

        if (event.getTestStep() instanceof PickleStepTestStep) {
            step = scenario.getSteps().getLast();
        } else if (!event.getResult().getStatus().isOk()) {
            final HookTestStep hook = ((HookTestStep) event.getTestStep());
            step = new Step();
            step.setId(stepId);
            step.setName("Hook: " + hook.getHookType().name());
            scenario.addStep(step);
        }
        if (null != step) {
            log.debug("Step ending: {}", step.getName());
            step.setCompletedAt(LocalDateTime.now());
            step.setStatus(TestStatus.parseStatus(event.getResult().getStatus().name()));
            step.setError(readStackTrace(event.getResult().getError()));
            testRun.updateStepStats(step.getStatus());
        }

    };

    private void writeLogEvent(WriteEvent event){
        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();

        Feature feature = testRun.getFeatures().get(featureId);
        Scenario scenario = feature.getScenarios().get(scenarioId);
        scenario.getSteps().getLast().getLogs().add(event.getText());
    }

    private Optional<io.cucumber.messages.types.Feature> parseFeature(final TestSourceRead event) {
        final GherkinParser parser = GherkinParser.builder()
                .includePickles(false)
                .includeSource(false)
                .build();
        try {
            URI uri = this.getClass().getClassLoader().getResource(event.getUri().getSchemeSpecificPart()).toURI();
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

    public static String readStackTrace(final Throwable e) {
        if(e!=null){
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
        return null;
    }


}