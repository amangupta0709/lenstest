# Test Run Resilience and Cleanup Implementation

This document describes the enhanced test run management system that provides backend resilience to application restarts, preventing test runs from being stuck in "IN_PROGRESS" state indefinitely.

## Overview

The solution provides:
1. **Process tracking** - Each test run is associated with the process that started it
2. **Heartbeat monitoring** - Active runs periodically update their "last seen" timestamp
3. **Automatic cleanup** - Orphaned and stuck runs are automatically detected and marked as failed
4. **Run type tracking** - Distinguishes between manual and scheduled runs

## Key Components

### 1. Enhanced TestRunEntity

The `TestRunEntity` now includes several new fields:

- `runType` (RunType enum): MANUAL or SCHEDULED
- `actualStartTime` (LocalDateTime): Precise start timestamp
- `scheduledRunId` (String): Reference ID for scheduled runs
- `processId` (String): Unique identifier of the process that started this run
- `lastHeartbeat` (LocalDateTime): Timestamp of last activity update

### 2. RunType Enum

```java
public enum RunType {
    MANUAL,    // Test run started manually by user
    SCHEDULED  // Test run started by scheduler
}
```

### 3. TestRunCleanupService

A Spring service responsible for:
- **Startup cleanup**: Detects orphaned runs from previous application instances
- **Periodic cleanup**: Finds and cleans up stuck runs (runs every 30 minutes)
- **Process validation**: Verifies if processes and runs are still active

#### Key Methods:

- `cleanupOrphanedRunsOnStartup()`: Runs on application startup via `@EventListener(ApplicationReadyEvent.class)`
- `cleanupStuckRuns()`: Scheduled to run every 30 minutes via `@Scheduled`
- `isTestRunStillActive(TestRunEntity run)`: Determines if a run is genuinely active

#### Cleanup Logic:

1. **Orphaned Run Detection**: Finds runs with `executionStage = "IN_PROGRESS"` and `processId != currentProcessId`
2. **Stuck Run Detection**: Finds runs that have been in progress longer than the maximum duration (2 hours by default)
3. **Activity Validation**: Before marking a run as failed, verifies it's not actually still active using:
   - Process ID validation (checks if the OS process still exists)
   - Heartbeat checking (recent activity within last 5 minutes)

### 4. Enhanced TestOrchestrator

The test orchestrator now:
- Creates `TestRunEntity` records when starting tests
- Starts periodic heartbeat updates (every 2 minutes)
- Properly handles cleanup on test completion or failure

#### Heartbeat Management:

- **Start**: `startHeartbeatUpdater(runId)` - begins periodic updates
- **Update**: `updateHeartbeat(runId)` - updates `lastHeartbeat` timestamp
- **Stop**: `stopHeartbeatUpdater(runId)` - cancels the heartbeat task

### 5. Repository Enhancements

New query methods in `TestRunEntityRepository`:

```java
// Find orphaned runs
Flux<TestRunEntity> findByExecutionStageAndProcessIdNot(String executionStage, String processId);

// Find potentially stuck runs
Flux<TestRunEntity> findByExecutionStageAndActualStartTimeBefore(String executionStage, LocalDateTime cutoffTime);

// Find runs by type and scheduled ID
Flux<TestRunEntity> findByRunType(RunType runType);
Flux<TestRunEntity> findByScheduledRunId(String scheduledRunId);
```

## How It Works

### Normal Operation

1. **Test Start**: `TestOrchestrator.executeTests()` is called
2. **Entity Creation**: A `TestRunEntity` is created with current process ID and start time
3. **Heartbeat Start**: Periodic heartbeat updates begin (every 2 minutes)
4. **Test Execution**: Tests run normally
5. **Cleanup**: Heartbeat stops and run status is updated to completion state

### Application Restart Scenario

1. **Previous Instance**: App crashes/restarts while tests are running
2. **New Instance Startup**: New process starts with different process ID
3. **Orphaned Detection**: `cleanupOrphanedRunsOnStartup()` finds runs with old process IDs
4. **Validation**: Checks if old processes are still alive (they're not)
5. **Cleanup**: Marks orphaned runs as "FAILED" with appropriate reason

### Stuck Run Detection

1. **Periodic Check**: Every 30 minutes, `cleanupStuckRuns()` executes
2. **Age Check**: Finds runs older than maximum duration (2 hours)
3. **Activity Check**: Validates if runs are truly active using:
   - OS process existence check
   - Recent heartbeat timestamps
4. **Selective Cleanup**: Only marks genuinely inactive runs as failed

## Configuration

### Timeouts and Intervals

In `TestRunCleanupService`:
- `MAX_RUN_DURATION_HOURS = 2`: Maximum time before considering a run stuck
- Cleanup interval: 30 minutes (1800000 ms)

In `TestOrchestrator`:
- Heartbeat interval: 2 minutes
- Heartbeat grace period: 5 minutes (in activity check)

### Process ID Generation

Uses `ManagementFactory.getRuntimeMXBean().getName()` which typically returns `PID@hostname` format, ensuring uniqueness across restarts and machines.

## Benefits

1. **Resilience**: No more permanently stuck "IN_PROGRESS" runs after application restarts
2. **Monitoring**: Clear visibility into run lifecycle and process ownership
3. **Flexibility**: Distinguishes between manual and scheduled runs
4. **Safety**: Conservative approach - only marks runs as failed when confident they're inactive
5. **Performance**: Minimal overhead with periodic background tasks

## Usage Examples

See `TestRunManagementExample.java` for practical examples of:
- Starting manual and scheduled runs
- Triggering cleanup operations
- Simulating orphaned runs for testing
- Checking run status and metadata

## Database Schema Impact

The implementation adds new fields to the test run collection:
- `runType` (default: "MANUAL")
- `actualStartTime` (LocalDateTime)
- `scheduledRunId` (optional String)
- `processId` (String)
- `lastHeartbeat` (optional LocalDateTime)

## Monitoring and Logging

The system provides detailed logging for:
- Process ID tracking
- Orphaned run detection
- Stuck run cleanup
- Heartbeat updates
- Process validation results

Log levels:
- `INFO`: Major operations (cleanup results, run creation)
- `DEBUG`: Detailed operations (heartbeat updates, process checks)
- `WARN`: Non-critical issues (heartbeat failures)
- `ERROR`: Critical failures (cleanup errors, entity save failures)

## Future Enhancements

Potential improvements:
1. **Configurable timeouts**: Make cleanup intervals and timeouts configurable via properties
2. **Metrics integration**: Add metrics for monitoring cleanup operations
3. **Advanced process tracking**: More sophisticated process validation
4. **Run cancellation**: API endpoints to manually cancel active runs
5. **Cluster coordination**: Enhanced multi-instance coordination for distributed deployments
