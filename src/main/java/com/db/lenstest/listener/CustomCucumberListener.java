package com.db.lenstest.listener;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.config.SpringContext;
import com.db.lenstest.lensDTO.*;
import com.db.lenstest.lensDTO.Step;
import com.db.lenstest.lensRepository.TestRunEntityRepository;
import com.db.lenstest.service.FeatureFilesParser;
import com.db.lenstest.service.TestRunCleanupService;
import com.db.lenstest.service.TestRunHeartbeatManager;
import io.cucumber.messages.types.Tag;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CustomCucumberListener implements ConcurrentEventListener {

    TestRun testRun = new TestRun();
    ConcurrentHashMap<String, Feature> featureMap = new ConcurrentHashMap<>();
    FeatureFilesParser featureFilesParser = new FeatureFilesParser();

    private final TestRunEntityRepository testRunEntityRepository = SpringContext.getBean(TestRunEntityRepository.class);
    private final TestRunCleanupService testRunCleanupService = SpringContext.getBean(TestRunCleanupService.class);
    private final TestRunHeartbeatManager heartbeatManager = SpringContext.getBean(TestRunHeartbeatManager.class);


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
    }

    private void runStarted(TestRunStarted event) {

        testRun.setExecutionStage(ExecutionStage.IN_PROGRESS);
        testRun.setFilterTag((String) TestRunContext.get("filterTag"));

        RunType runType = (RunType) TestRunContext.get("runType");
        String scheduledRunId = (String) TestRunContext.get("scheduledRunId");
        String processId = (String) TestRunContext.get("processId");
        
        if (runType == null) runType = RunType.MANUAL;
        if (processId == null) processId = testRunCleanupService.getCurrentProcessId();

        testRun.initialValues(runType, scheduledRunId, processId);
        
        log.info("Initialized test run with runType: {}, processId: {}, scheduledRunId: {}", 
                runType, processId, scheduledRunId);
        
        // Save to database and start heartbeat
        testRunEntityRepository
                .save(testRun.toEntity())
                .doOnSuccess(testRunEntity -> {
                    testRun.setId(testRunEntity.getId());
                    log.info("Created test run entity with ID: " + testRunEntity.getId());
                    
                    // Start heartbeat monitoring - this updates the DTO's heartbeat field
                    heartbeatManager.startHeartbeat(testRunEntity.getId(), () -> {
                        testRun.updateHeartbeat();
                        log.debug("Updated heartbeat for run: " + testRunEntity.getId());
                    });
                })
                .subscribe();
    }

    private void runFinished(TestRunFinished event) {
        log.info("Test run finished for run ID: " + testRun.getId());
        
        // Stop heartbeat monitoring
        if (testRun.getId() != null) {
            heartbeatManager.stopHeartbeat(testRun.getId());
        }
        
        // Finalize test run
        testRun.setCompletedAt(LocalDateTime.now());
        testRun.setExecutionStage(ExecutionStage.FINISHED);
        
        // Final save and publish
        testRunEntityRepository
                .save(testRun.toEntity())
                .doOnSuccess(testRunEntity -> {
                    log.info("Test run completed and saved: " + testRunEntity.getId());
                    ResultPublisher.publish(testRunEntity);
                })
                .subscribe();
    }

    private void featureRead(TestSourceRead event) {
        Optional<io.cucumber.messages.types.Feature> container = featureFilesParser.parseFeature(event);
        container.ifPresent(featureCucumber -> {
            final Stream<String> tags = featureCucumber.getTags().stream().map(Tag::getName);
            Feature feature = new Feature();
            feature.setId(event.getUri().getSchemeSpecificPart());
            feature.setName("Feature: "+featureCucumber.getName());
            feature.setDescription(featureCucumber.getDescription());
            feature.getTags().addAll(tags.collect(Collectors.toSet()));
            feature.setStatus(TestStatus.PASSED);

            featureMap.put(feature.getId(), feature);
        });
    }

    private void ScenarioStarted(TestCaseStarted event) {
        log.debug("Scenario start: {}", event.getTestCase().getName());

        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();

        testRun.addFeature(featureMap.get(featureId));
        testRun.getFeatureStats().getTotal().incrementAndGet();
        testRun.getFeatureStats().getPassed().incrementAndGet();

        Scenario scenario = new Scenario();
        scenario.setId(scenarioId);
        scenario.setName(event.getTestCase().getKeyword() + ": " + event.getTestCase().getName());
        scenario.getTags().addAll(event.getTestCase().getTags());

        testRun.getFeatures().get(featureId).addScenario(scenario);
    }

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
    }

    private void stepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep pickle) {
            log.debug("Step starting: {}", pickle.getStep().getText());

            String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
            UUID scenarioId = event.getTestCase().getId();
            UUID stepId = event.getTestStep().getId();

            Feature feature = testRun.getFeatures().get(featureId);
            Scenario scenario = feature.getScenarios().get(scenarioId);

            Step step = new Step();
            step.setId(stepId);
            step.setName(pickle.getStep().getKeyword() + pickle.getStep().getText());

            StepArgument argument = pickle.getStep().getArgument();
            if (argument instanceof DataTableArgument dataTableArgument) {
                step.setDataTable(dataTableArgument.cells());
            }

            scenario.addStep(step);
        }
    }

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
            for(String logString : StepLogCollector.get()) {
                Log logDTO = new Log();
                logDTO.setValue(logString);
                logDTO.setShowReport(false);
                step.getLogs().add(logDTO);
            }
            StepLogCollector.clear();
            testRun.updateStepStats(step.getStatus());
        }

    }

    private void writeLogEvent(WriteEvent event){
        Log logDTO = new Log();
        logDTO.setValue(event.getText());
        logDTO.setShowReport(true);

        String featureId = event.getTestCase().getUri().getSchemeSpecificPart();
        UUID scenarioId = event.getTestCase().getId();

        Feature feature = testRun.getFeatures().get(featureId);
        Scenario scenario = feature.getScenarios().get(scenarioId);
        scenario.getSteps().getLast().getLogs().add(logDTO);
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