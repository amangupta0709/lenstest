package com.db.lenstest.service;

//import com.db.lenstest.listener.ExtentReportListener;
//import com.db.lenstest.listener.TestExecutionListener;
import com.db.lenstest.listener.ExtentCucumberListener;
import com.db.lenstest.runner.TestCucumberRunner;
import io.cucumber.testng.TestNGCucumberRunner;
import org.testng.TestNG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TestOrchestrator {

    private final ExecutorService executor;
//    private final ExtentCucumberListener reportListener;
//    private final TestExecutionListener testListener;
//    private final ExtentReportListener reportListener;

    @Autowired
    public TestOrchestrator() {
        this.executor = Executors.newFixedThreadPool(10);
//        this.testListener = testListener;
//        this.reportListener = reportListener;
    }

    public void executeTests(String tag) {
        executor.submit(() -> {
            TestNG testNG = new TestNG();
            testNG.setTestClasses(new Class[]{TestCucumberRunner.class});

            testNG.setUseDefaultListeners(false); // Disables TestNG's default reports

            // Register listeners
//            testNG.addListener(testListener);
//            testNG.addListener(reportListener);
//            testNG.addListener(reportListener);

            System.setProperty("cucumber.filter.tags", "@" + tag);
            System.setProperty("cucumber.execution.parallel.enabled", "true");

            testNG.run();
        });
    }
}
