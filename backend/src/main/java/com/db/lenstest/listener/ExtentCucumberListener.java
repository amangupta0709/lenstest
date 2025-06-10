package com.db.lenstest.listener;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.gherkin.model.*;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.FileUtil;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.HookTestStep;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExtentCucumberListener implements EventListener {

    private ExtentReports extent;


    private Map<String, ExtentTest> featureTests = new HashMap<>();
    private Map<String, String> featureNames = new HashMap<>();
    private ThreadLocal<ExtentTest> scenario = new ThreadLocal<>();
    private ThreadLocal<ExtentTest> step = new ThreadLocal<>();


    public ExtentCucumberListener() {
    };


    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // TODO Auto-generated method stub


        /*
         * :: is method reference , so this::collectTag means collectTags method in
         * 'this' instance. Here we says runStarted method accepts or listens to
         * TestRunStarted event type
         */
        publisher.registerHandlerFor(TestRunStarted.class, this::runStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::runFinished);
        publisher.registerHandlerFor(TestSourceRead.class, this::featureRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::ScenarioStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinished);


    };


    /*
     * Here we set argument type as TestRunStarted if you set anything else then the
     * corresponding register shows error as it doesn't have a listner method that
     * accepts the type specified in TestRunStarted.class
     */


    // Here we create the reporter
    private void runStarted(TestRunStarted event) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportPath = "test-output/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
        htmlReporter.config().setDocumentTitle("LensTest");
        htmlReporter.config().setReportName("Lens Report");
//        try {
////            htmlReporter.loadXMLConfig(new File(getClass().getClassLoader().getResource("extent-config.xml").toURI()));
//            String css = new String(getClass().getClassLoader().getResourceAsStream("extent.css").readAllBytes());
//            String js = new String(getClass().getClassLoader().getResourceAsStream("extent.js").readAllBytes());
//            htmlReporter.config().setCss(css);
//            htmlReporter.config().setJs(js);
//        } catch (Exception ignored){
//
//        }
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    };


    // TestRunFinished event is triggered when all feature file executions are
    // completed
    private void runFinished(TestRunFinished event) {
        extent.flush();
    };


    // This event is triggered when feature file is read
    // here we create the feature node
    private void featureRead(TestSourceRead event) {
        String feature = event.getUri().toString().split("/")[1];
        String source = event.getSource();
        String featureName = source.replaceFirst("(?s).*Feature:\\s([^\n]*)\n.*", "$1").trim();
        String featureDescription = extractFeatureDescription(source);



        ExtentTest test = extent.createTest(Feature.class,featureName,featureDescription);
        featureTests.putIfAbsent(feature, test);
        featureNames.putIfAbsent(feature, featureName);
    };


    // This event is triggered when Test Case is started
    // here we create the scenario node
    private void ScenarioStarted(TestCaseStarted event) {
        String feature = event.getTestCase().getUri().toString().split("/")[1];
        ExtentTest scenarioTest = featureTests.get(feature).createNode(Scenario.class,event.getTestCase().getName());
        for(String tag : event.getTestCase().getTags()){
            scenarioTest.assignCategory(tag);
        }
        scenario.set(scenarioTest);
    };


    // step started event
    // here we creates the test node
    private void stepStarted(TestStepStarted event) {
        String stepName = " ";
        String keyword = "Triggered the hook :";


        // We checks whether the event is from a hook or step
        if (event.getTestStep() instanceof PickleStepTestStep) {
            // TestStepStarted event implements PickleStepTestStep interface
            // Which have additional methods to interact with the event object
            // So we have to cast TestCase object to get those methods
            PickleStepTestStep steps = (PickleStepTestStep) event.getTestStep();
            stepName = steps.getStep().getText();
            keyword = steps.getStep().getKeyword();


        } else {
            // Same with HookTestStep
            HookTestStep hoo = (HookTestStep) event.getTestStep();
            stepName = hoo.getHookType().name();
        }

        if(keyword.equals("Given ")) {
            step.set(scenario.get().createNode(Given.class, keyword + stepName));
        }
        else if(keyword.equals("When ")) {
            step.set(scenario.get().createNode(When.class, keyword + stepName));
        }
        else if(keyword.equals("Then ")) {
            step.set(scenario.get().createNode(Then.class, keyword + stepName));
        } else if(keyword.equals("And ")) {
            step.set(scenario.get().createNode(And.class,keyword + stepName));
        } else {
            step.set(scenario.get().createNode(keyword + stepName));
        }
    };


    // This is triggered when TestStep is finished
    private void stepFinished(TestStepFinished event) {
        if (event.getResult().getStatus().toString().equals("PASSED")) {
            step.get().log(Status.PASS,"");
        } else if (event.getResult().getStatus().toString().equals("SKIPPED")) {
            step.get().log(Status.SKIP,"Step skipped");
        } else {
            step.get().log(Status.FAIL,event.getResult().getError());
        }
        extent.flush();
    };

    private String extractFeatureDescription(String source) {
        int featureIndex = source.indexOf("Feature:");  // Find where "Feature:" begins
        int scenarioIndex = source.indexOf("Scenario:"); // Find where "Scenario:" begins

        if (featureIndex != -1) {
            String text = source.substring(featureIndex + 8, scenarioIndex == -1 ? source.length() : scenarioIndex).trim();
            String description = String.join("", Arrays.copyOfRange(text.split("\n"), 1, text.split("\n").length)).trim();

            return description;
        }

        return "";
    }


}