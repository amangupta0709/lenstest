package com.db.lenstest.service;

import com.db.lenstest.lensDTO.RunType;
import com.db.lenstest.lensEntity.TestRunEntity;
import com.db.lenstest.lensRepository.TestRunEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Example class demonstrating the enhanced test run management functionality
 * This shows how the system now handles:
 * 1. Process tracking for test runs through TestRun DTO and listener
 * 2. Heartbeat monitoring via TestRunHeartbeatManager (no DB race conditions)
 * 3. Cleanup of orphaned and stuck runs via TestRunCleanupService
 * 4. Run type tracking (MANUAL vs SCHEDULED) while preserving thread-safe collections
 */
@Component
@Slf4j
public class TestRunManagementExample {

    @Autowired
    private TestOrchestrator testOrchestrator;
    
    @Autowired
    private TestRunCleanupService cleanupService;
    
    @Autowired
    private TestRunEntityRepository testRunEntityRepository;

    /**
     * Example of starting a manual test run
     */
    public void startManualTestRun() {
        log.info("=== Starting Manual Test Run ===");
        log.info("Current Process ID: " + cleanupService.getCurrentProcessId());
        
        // This will create a test run entity with:
        // - runType = MANUAL
        // - processId = current process ID
        // - heartbeat updates every 2 minutes
        testOrchestrator.executeTests("@smoke");
    }
    
    /**
     * Example of starting a scheduled test run
     */
    public void startScheduledTestRun() {
        log.info("=== Starting Scheduled Test Run ===");
        String scheduledRunId = "scheduled-run-001";
        
        // This will create a test run entity with:
        // - runType = SCHEDULED
        // - scheduledRunId = provided ID
        // - processId = current process ID
        // - heartbeat updates every 2 minutes
        testOrchestrator.executeTests("@regression", RunType.SCHEDULED, scheduledRunId);
    }
    
    /**
     * Example of manually triggering cleanup operations
     */
    public void demonstrateCleanupOperations() {
        log.info("=== Demonstrating Cleanup Operations ===");
        
        // This would normally run automatically on application startup
        cleanupService.cleanupOrphanedRunsOnStartup();
        
        // This would normally run every 30 minutes as a scheduled task
        cleanupService.cleanupStuckRuns();
    }
    
    /**
     * Example of creating a simulated orphaned run for testing
     */
    public void simulateOrphanedRun() {
        log.info("=== Simulating Orphaned Run for Testing ===");
        
        try {
            TestRunEntity orphanedRun = new TestRunEntity();
            orphanedRun.setFilterTag("@test");
            orphanedRun.setRunType(RunType.MANUAL);
            orphanedRun.setProcessId("12345@old-hostname"); // Different process ID
            orphanedRun.setStartedAt(LocalDateTime.now().minusHours(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            orphanedRun.setExecutionStage("IN_PROGRESS");
            orphanedRun.setLastHeartbeat(LocalDateTime.now().minusHours(2)); // Last heartbeat 2 hours ago
            
            TestRunEntity savedRun = testRunEntityRepository.save(orphanedRun).block();
            log.info("Created simulated orphaned run with ID: " + savedRun.getId());
            
            // Now cleanup should detect and mark this as failed
            cleanupService.cleanupOrphanedRunsOnStartup();
            
        } catch (Exception e) {
            log.error("Error simulating orphaned run: " + e.getMessage());
        }
    }
    
    /**
     * Example of checking run status
     */
    public void checkRunStatus(String runId) {
        try {
            TestRunEntity run = testRunEntityRepository.findById(runId).block();
            if (run != null) {
                log.info("=== Run Status for ID: " + runId + " ===");
                log.info("Execution Stage: " + run.getExecutionStage());
                log.info("Run Type: " + run.getRunType());
                log.info("Process ID: " + run.getProcessId());
                log.info("Started At: " + run.getStartedAt());
                log.info("Last Heartbeat: " + run.getLastHeartbeat());
                log.info("Is from current process: " + cleanupService.isRunFromCurrentProcess(run));
            } else {
                log.warn("No run found with ID: " + runId);
            }
        } catch (Exception e) {
            log.error("Error checking run status: " + e.getMessage());
        }
    }
}
