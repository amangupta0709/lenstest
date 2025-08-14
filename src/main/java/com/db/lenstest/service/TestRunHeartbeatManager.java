package com.db.lenstest.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages heartbeat updates for active test runs
 * Works with TestRun DTOs in the listener to avoid database race conditions
 */
@Service
@Slf4j
public class TestRunHeartbeatManager {

    private final ScheduledExecutorService heartbeatExecutor;
    private final ConcurrentHashMap<String, HeartbeatTask> activeHeartbeats;

    public TestRunHeartbeatManager() {
        this.heartbeatExecutor = Executors.newScheduledThreadPool(5);
        this.activeHeartbeats = new ConcurrentHashMap<>();
    }

    /**
     * Start heartbeat tracking for a test run
     * @param runId The test run ID
     * @param heartbeatCallback Callback to update the TestRun DTO's heartbeat
     */
    public void startHeartbeat(String runId, Runnable heartbeatCallback) {
        if (runId == null || heartbeatCallback == null) {
            log.warn("Cannot start heartbeat with null runId or callback");
            return;
        }

        // Stop any existing heartbeat for this run
        stopHeartbeat(runId);

        ScheduledFuture<?> scheduledTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                heartbeatCallback.run();
                log.debug("Updated heartbeat for run: " + runId);
            } catch (Exception e) {
                log.warn("Failed to update heartbeat for run " + runId + ": " + e.getMessage());
            }
        }, 0, 2, TimeUnit.MINUTES); // Initial delay 0, then every 2 minutes

        HeartbeatTask task = new HeartbeatTask(scheduledTask, heartbeatCallback);
        activeHeartbeats.put(runId, task);
        
        log.info("Started heartbeat monitoring for test run: " + runId);
    }

    /**
     * Stop heartbeat tracking for a test run
     */
    public void stopHeartbeat(String runId) {
        HeartbeatTask task = activeHeartbeats.remove(runId);
        if (task != null) {
            task.getScheduledFuture().cancel(false);
            log.info("Stopped heartbeat monitoring for test run: " + runId);
        }
    }

    /**
     * Manually trigger a heartbeat update for a run
     */
    public void triggerHeartbeat(String runId) {
        HeartbeatTask task = activeHeartbeats.get(runId);
        if (task != null) {
            try {
                task.getHeartbeatCallback().run();
                log.debug("Manually triggered heartbeat for run: " + runId);
            } catch (Exception e) {
                log.warn("Failed to manually trigger heartbeat for run " + runId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check if a run has active heartbeat monitoring
     */
    public boolean hasActiveHeartbeat(String runId) {
        return activeHeartbeats.containsKey(runId);
    }

    /**
     * Get count of actively monitored runs
     */
    public int getActiveHeartbeatCount() {
        return activeHeartbeats.size();
    }

    /**
     * Stop all active heartbeats (useful for shutdown)
     */
    public void stopAllHeartbeats() {
        log.info("Stopping all active heartbeats (" + activeHeartbeats.size() + " runs)");
        activeHeartbeats.values().forEach(task -> task.getScheduledFuture().cancel(false));
        activeHeartbeats.clear();
    }

    /**
     * Container for heartbeat task information
     */
    @Getter
    private static class HeartbeatTask {
        private final ScheduledFuture<?> scheduledFuture;
        private final Runnable heartbeatCallback;

        public HeartbeatTask(ScheduledFuture<?> scheduledFuture, Runnable heartbeatCallback) {
            this.scheduledFuture = scheduledFuture;
            this.heartbeatCallback = heartbeatCallback;
        }

    }
}
