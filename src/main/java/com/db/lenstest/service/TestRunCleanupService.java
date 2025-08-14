package com.db.lenstest.service;

import com.db.lenstest.lensDTO.ExecutionStage;
import com.db.lenstest.lensEntity.TestRunEntity;
import com.db.lenstest.lensRepository.TestRunEntityRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TestRunCleanupService {

    @Autowired
    private TestRunEntityRepository testRunEntityRepository;
    
    @Getter
    private final String currentProcessId;
    private static final int MAX_RUN_DURATION_HOURS = 2; // Max time before considering a run stuck
    
    public TestRunCleanupService() {
        // Generate unique process ID for this application instance
        this.currentProcessId = ManagementFactory.getRuntimeMXBean().getName();
        log.info("TestRunCleanupService initialized with Process ID: {}", currentProcessId);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOrphanedRunsOnStartup() {
        log.info("Checking for orphaned test runs from previous application instances...");
        
        // Find all IN_PROGRESS runs that were not started by current process
        List<TestRunEntity> orphanedRuns = testRunEntityRepository
            .findByExecutionStageAndProcessIdNot(ExecutionStage.IN_PROGRESS.name(), currentProcessId)
            .collectList()
            .block();
            
        if (orphanedRuns != null && !orphanedRuns.isEmpty()) {
            log.info("Found {} orphaned runs. Marking as FAILED...",orphanedRuns.size());
            
            for (TestRunEntity run : orphanedRuns) {
                markRunAsFailed(run, "Application restart - previous instance terminated");
            }
        } else {
            log.info("No orphaned runs found.");
        }
    }
    
    // Check for stuck runs every 30 minutes
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void cleanupStuckRuns() {
        log.info("Checking for stuck test runs...");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(MAX_RUN_DURATION_HOURS);
        
        List<TestRunEntity> stuckRuns = testRunEntityRepository
            .findByExecutionStageAndStartedAtBefore(ExecutionStage.IN_PROGRESS.name(), cutoffTime)
            .collectList()
            .block();
            
        if (stuckRuns != null && !stuckRuns.isEmpty()) {
            log.info("Found {} potentially stuck runs. Verifying if still active...",stuckRuns.size());
            
            for (TestRunEntity run : stuckRuns) {
                if (isTestRunStillActive(run)) {
                    log.info("Run {} is still active, skipping cleanup",run.getId());
                } else {
                    log.info("Run {} is no longer active, marking as failed",run.getId());
                    markRunAsFailed(run, "Run exceeded maximum duration (" + MAX_RUN_DURATION_HOURS + " hours) and is no longer active");
                }
            }
        }
    }
    
    private void markRunAsFailed(TestRunEntity run, String reason) {
        try {
            run.setExecutionStage("FAILED");
            run.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            testRunEntityRepository.save(run).block();
            
            log.info("Marked run " + run.getId() + " as FAILED. Reason: " + reason);
        } catch (Exception e) {
            log.error("Error marking run " + run.getId() + " as failed: " + e.getMessage());
        }
    }

    // Helper method to check if a run belongs to current process
    public boolean isRunFromCurrentProcess(TestRunEntity run) {
        return currentProcessId.equals(run.getProcessId());
    }
    
    /**
     * Check if a test run is still actively running
     * This method combines multiple checks to determine if a test run is truly active
     */
    private boolean isTestRunStillActive(TestRunEntity run) {
        // First check: If run is from current process, it might still be active
        if (isRunFromCurrentProcess(run)) {
            // Check if the process is still alive and if there are active executions
            return isCurrentProcessAlive() && hasActiveExecutions(run);
        }
        
        // For runs from other processes, check if the process ID is still active
        if (run.getProcessId() != null) {
            return isProcessAlive(run.getProcessId());
        }
        
        // If no process ID, assume it's not active
        return false;
    }
    
    /**
     * Check if current application process is still alive
     */
    private boolean isCurrentProcessAlive() {
        // Current process is alive if we're executing this code
        return true;
    }
    
    /**
     * Check if a specific process ID is still running
     */
    private boolean isProcessAlive(String processId) {
        try {
            // Extract PID from process ID (format is usually PID@hostname)
            String[] parts = processId.split("@");
            if (parts.length > 0) {
                String pid = parts[0];
                
                // Use system command to check if process exists
                Process process = Runtime.getRuntime().exec(new String[]{"ps", "-p", pid});
                process.waitFor();
                
                // Process exists if exit code is 0
                return process.exitValue() == 0;
            }
        } catch (Exception e) {
            log.debug("Error checking if process " + processId + " is alive: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if there are active executions for a test run
     * This can be enhanced to check actual execution status
     */
    private boolean hasActiveExecutions(TestRunEntity run) {
        // For now, we'll assume that if a run is from the current process
        // and the process is alive, there might be active executions

        // if run was updated recently (within last 5 minutes), consider it active
        if (run.getLastHeartbeat() != null) {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            return run.getLastHeartbeat().isAfter(fiveMinutesAgo);
        }

        return false;
    }
}
