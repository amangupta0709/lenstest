package com.db.lenstest.service;

import com.db.lenstest.lensDTO.RunType;
import com.db.lenstest.listener.TestRunContext;
import com.db.lenstest.runner.TestCucumberRunner;
import lombok.extern.slf4j.Slf4j;
import org.testng.TestNG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class TestOrchestrator {

    private final ExecutorService executor;
    
    @Autowired
    private TestRunCleanupService cleanupService;

    public TestOrchestrator() {
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void executeTests(String filterTag) {
        executeTests(filterTag, RunType.MANUAL, null);
    }
    
    public void executeTests(String filterTag, RunType runType, String scheduledRunId) {
        executor.submit(() -> {
            try {
                log.info("Starting test execution with runType: " + runType + ", scheduledRunId: " + scheduledRunId);
                
                // Set execution context for the listener to use
                TestRunContext.put("filterTag", filterTag);
                TestRunContext.put("runType", runType);
                TestRunContext.put("scheduledRunId", scheduledRunId);
                TestRunContext.put("processId", cleanupService.getCurrentProcessId());
                
                TestNG testNG = new TestNG();
                testNG.setTestClasses(new Class[]{TestCucumberRunner.class});
                testNG.setUseDefaultListeners(false);
//                System.setProperty("cucumber.execution.parallel.enabled", "true");
                testNG.run();
                
                log.info("Test execution completed successfully");
                
            } catch (Exception e) {
                log.error("Error executing tests: " + e.getMessage(), e);
            }
        });
    }
    
}
