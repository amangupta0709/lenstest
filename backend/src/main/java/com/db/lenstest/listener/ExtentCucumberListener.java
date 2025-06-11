package com.db.lenstest.listener;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.domain.*;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Tag;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExtentCucumberListener implements ConcurrentEventListener {

    Build build = new Build();
    Map<String,Test> featureTests = new ConcurrentHashMap<>();
    Map<UUID,Test> scenarioTests = new ConcurrentHashMap<>();
    Map<UUID,Test> stepTests = new ConcurrentHashMap<>();
    Set<String> featureBuildUpdated = new ConcurrentSkipListSet<>();


    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::runStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::runFinished);
        publisher.registerHandlerFor(TestSourceRead.class, this::featureRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::ScenarioStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::ScenarioFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinished);
    };

    private void runStarted(TestRunStarted event) {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String reportPath = "test-output/ExtentReport_" + timestamp + ".html";
//
//        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
//        htmlReporter.config().setDocumentTitle("LensTest");
//        htmlReporter.config().setReportName("Lens Report");
//        try {
////            htmlReporter.loadXMLConfig(new File(getClass().getClassLoader().getResource("extent-config.xml").toURI()));
//            String css = new String(getClass().getClassLoader().getResourceAsStream("extent.css").readAllBytes());
//            String js = new String(getClass().getClassLoader().getResourceAsStream("extent.js").readAllBytes());
//            htmlReporter.config().setCss(css);
//            htmlReporter.config().setJs(js);
//        } catch (Exception ignored){
//
//        }
//        extent = new ExtentReports();
//        extent.attachReporter(htmlReporter);
        build.setExecutionStage(ExecutionStage.IN_PROGRESS);
//        buildRepository.save(build).subscribe();
    };


    // TestRunFinished event is triggered when all feature file executions are
    // completed
    private void runFinished(TestRunFinished event) {
        build.setCompletedAt(System.currentTimeMillis());
        build.setDuration(build.getCompletedAt()- build.getStartedAt());
        build.setExecutionStage(ExecutionStage.FINISHED);
        ResultPublisher.publishOnBuildCompletion(build);
    };


    // This event is triggered when feature file is read
    // here we create the feature node
    private void featureRead(TestSourceRead event) {
//        String feature = event.getUri().toString().split("/")[1];
//        String source = event.getSource();
//        String featureName = source.replaceFirst("(?s).*Feature:\\s([^\n]*)\n.*", "$1").trim();
//        String featureDescription = extractFeatureDescription(source);
//
//
//
//        ExtentTest test = extent.createTest(Feature.class,featureName,featureDescription);
//        featureTests.putIfAbsent(feature, test);
//        featureNames.putIfAbsent(feature, featureName);
        Optional<Feature> container = parseFeature(event);
        container.ifPresent(feature -> {
            final Stream<String> tags = feature.getTags().stream().map(Tag::getName);
            Test test = new Test();
            test.setName("Feature: "+feature.getName());
            test.setLevel(TestLevel.FEATURE);
            test.setBuildId(build.getId());
            test.getTags().addAll(tags.collect(Collectors.toSet()));

            featureTests.put(event.getUri().toString(), test);
        });
    };


    // This event is triggered when Test Case is started
    // here we create the scenario node
    private void ScenarioStarted(TestCaseStarted event) {
//        String feature = event.getTestCase().getUri().toString().split("/")[1];
//        ExtentTest scenarioTest = featureTests.get(feature).createNode(Scenario.class,event.getTestCase().getName());
//        for(String tag : event.getTestCase().getTags()){
//            scenarioTest.assignCategory(tag);
//        }
//        scenario.set(scenarioTest);
        log.debug("Scenario start: {}", event.getTestCase().getName());
        Test scenario = new Test();
        scenario.setName(event.getTestCase().getKeyword() + ": " + event.getTestCase().getName());
        scenario.setLevel(TestLevel.SCENARIO);
        scenario.setBuildId(build.getId());
        scenario.getTags().addAll(event.getTestCase().getTags());

        featureTests.get(event.getTestCase().getUri().toString()).addChild(scenario);
        scenarioTests.put(event.getTestCase().getId(), scenario);
    };

    private void ScenarioFinished(TestCaseFinished event) {
//        String feature = event.getTestCase().getUri().toString().split("/")[1];
//        ExtentTest scenarioTest = featureTests.get(feature).createNode(Scenario.class,event.getTestCase().getName());
//        for(String tag : event.getTestCase().getTags()){
//            scenarioTest.assignCategory(tag);
//        }
//        scenario.set(scenarioTest);
        log.debug("Scenario end: {}", event.getTestCase().getName());
        Test scenario = scenarioTests.get(event.getTestCase().getId());

        scenario.setStatus(TestStatus.parseStatus(event.getResult().getStatus().name()));
        scenario.complete(Optional.empty());
        Test feature = scenario.getParent();
        feature.complete(Optional.empty());
//        testRepository.save(scenario)
//                .doOnSuccess(saved -> resultPublisher.publishOnScenarioCompletion(scenario))
//                .subscribe();
        build.updateStats(TestLevel.SCENARIO,scenario.getStatus());
        if(!featureBuildUpdated.contains(event.getTestCase().getUri().toString())){
            build.updateStats(TestLevel.FEATURE,feature.getStatus());
            featureBuildUpdated.add(event.getTestCase().getUri().toString());
        }
        for(String tag : event.getTestCase().getTags()){
            build.updateTagStats(tag,scenario.getStatus());
        }
        build.setDuration(System.currentTimeMillis()-build.getStartedAt());
        ResultPublisher.publishOnScenarioCompletion(scenario,build);
    };


    // step started event
    // here we creates the test node
    private void stepStarted(TestStepStarted event) {
//        String stepName = " ";
//        String keyword = "Triggered the hook :";
//
//
//        // We checks whether the event is from a hook or step
//        if (event.getTestStep() instanceof PickleStepTestStep) {
//            // TestStepStarted event implements PickleStepTestStep interface
//            // Which have additional methods to interact with the event object
//            // So we have to cast TestCase object to get those methods
//            PickleStepTestStep steps = (PickleStepTestStep) event.getTestStep();
//            stepName = steps.getStep().getText();
//            keyword = steps.getStep().getKeyword();
//
//
//        } else {
//            // Same with HookTestStep
//            HookTestStep hoo = (HookTestStep) event.getTestStep();
//            stepName = hoo.getHookType().name();
//        }
//
//        if(keyword.equals("Given ")) {
//            step.set(scenario.get().createNode(Given.class, keyword + stepName));
//        }
//        else if(keyword.equals("When ")) {
//            step.set(scenario.get().createNode(When.class, keyword + stepName));
//        }
//        else if(keyword.equals("Then ")) {
//            step.set(scenario.get().createNode(Then.class, keyword + stepName));
//        } else if(keyword.equals("And ")) {
//            step.set(scenario.get().createNode(And.class,keyword + stepName));
//        } else {
//            step.set(scenario.get().createNode(keyword + stepName));
//        }
        if (event.getTestStep() instanceof PickleStepTestStep) {
            final PickleStepTestStep pickle = ((PickleStepTestStep) event.getTestStep());
            log.debug("Step starting: {}", pickle.getStep().getText());
            Test step = new Test();
            step.setName(pickle.getStep().getKeyword() + pickle.getStep().getText());
            step.setBuildId(build.getId());
            step.setLevel(TestLevel.STEP);

            scenarioTests.get(event.getTestCase().getId()).addChild(step);
            stepTests.put(event.getTestStep().getId(), step);
        }
    };


    // This is triggered when TestStep is finished
    private void stepFinished(TestStepFinished event) {
//        if (event.getResult().getStatus().toString().equals("PASSED")) {
//            step.get().log(Status.PASS,"");
//        } else if (event.getResult().getStatus().toString().equals("SKIPPED")) {
//            step.get().log(Status.SKIP,"Step skipped");
//        } else {
//            step.get().log(Status.FAIL,event.getResult().getError());
//        }
//        extent.flush();
        Test step = null;
        if (event.getTestStep() instanceof PickleStepTestStep) {
            step = stepTests.get(event.getTestStep().getId());
        } else if (!event.getResult().getStatus().isOk()) {
            final HookTestStep hook = ((HookTestStep) event.getTestStep());
            step = new Test();
            step.setName("Hook: " + hook.getHookType().name());
            scenarioTests.get(event.getTestCase().getId()).addChild(step);
        }
        if (null != step) {
            log.debug("Step ending: {}", step.getName());
            step.setStatus(TestStatus.parseStatus(event.getResult().getStatus().name()));
            step.complete(Optional.ofNullable(event.getResult().getError()));
            build.updateStats(TestLevel.STEP,step.getStatus());
        }

    };

    private Optional<Feature> parseFeature(final TestSourceRead event) {
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

    private static Map<String, String> createTestResult(String testCaseName, String status, String message) {
        Map<String, String> result = new HashMap<>();
        result.put("testCaseName", testCaseName);
        result.put("status", status);
        result.put("message", message);
        return result;
    }


}