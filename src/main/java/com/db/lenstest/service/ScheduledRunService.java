package com.db.lenstest.service;

import com.db.lenstest.lensDTO.ScheduledRunRequest;
import com.db.lenstest.lensEntity.ScheduledRunEntity;
import com.db.lenstest.lensRepository.ScheduledRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduledRunService {

    @Autowired
    private ScheduledRunRepository scheduledRunRepository;

    @Autowired
    private TestOrchestrator testOrchestrator;

    @Autowired
    private TaskScheduler taskScheduler;

    // Map to keep track of scheduled tasks
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public Mono<ScheduledRunEntity> createScheduledRun(ScheduledRunRequest request) {
        ScheduledRunEntity entity = new ScheduledRunEntity();
        entity.setName(request.getName());
        entity.setIncludeTags(request.getIncludeTags());
        entity.setExcludeTags(request.getExcludeTags());
        entity.setCronExpression(request.getCronExpression());
        
        return scheduledRunRepository.save(entity)
                .doOnSuccess(this::scheduleTask);
    }

    public Flux<ScheduledRunEntity> getAllScheduledRuns() {
        return scheduledRunRepository.findAll();
    }

    public Flux<ScheduledRunEntity> getActiveScheduledRuns() {
        return scheduledRunRepository.findByActiveTrue();
    }

    public Mono<Void> deleteScheduledRun(String id) {
        return scheduledRunRepository.findById(id)
                .doOnNext(entity -> {
                    // Cancel the scheduled task
                    ScheduledFuture<?> task = scheduledTasks.get(id);
                    if (task != null) {
                        task.cancel(false);
                        scheduledTasks.remove(id);
                    }
                })
                .then(scheduledRunRepository.deleteById(id));
    }

    public Mono<ScheduledRunEntity> toggleScheduledRun(String id, boolean active) {
        return scheduledRunRepository.findById(id)
                .flatMap(entity -> {
                    entity.setActive(active);
                    if (active) {
                        scheduleTask(entity);
                    } else {
                        // Cancel the scheduled task
                        ScheduledFuture<?> task = scheduledTasks.get(id);
                        if (task != null) {
                            task.cancel(false);
                            scheduledTasks.remove(id);
                        }
                    }
                    return scheduledRunRepository.save(entity);
                });
    }

    private void scheduleTask(ScheduledRunEntity entity) {
        if (!entity.isActive()) {
            return;
        }

        try {
            CronTrigger cronTrigger = new CronTrigger(entity.getCronExpression());
            
            Runnable task = () -> {
                System.out.println("Executing scheduled run: " + entity.getName());
                
                // Create the test expression from tags
                String testExpression = createTestExpression(entity.getIncludeTags(), entity.getExcludeTags());
                
                // Execute the test
                testOrchestrator.executeTests(testExpression);
                
                // Update last run information
                entity.setLastRunAt(LocalDateTime.now());
                scheduledRunRepository.save(entity).subscribe();
            };

            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, cronTrigger);
            scheduledTasks.put(entity.getId(), scheduledTask);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid cron expression for scheduled run " + entity.getName() + ": " + e.getMessage());
        }
    }

    private String createTestExpression(java.util.List<String> includeTags, java.util.List<String> excludeTags) {
        String includeExpression = includeTags != null && !includeTags.isEmpty() 
            ? String.join(" or ", includeTags) : "";
        String excludeExpression = excludeTags != null && !excludeTags.isEmpty() 
            ? String.join(" or ", excludeTags) : "";

        String finalTagExpression = "";

        if (!includeExpression.isEmpty() && !excludeExpression.isEmpty()) {
            finalTagExpression = includeExpression + " and not (" + excludeExpression + ")";
        } else if (!includeExpression.isEmpty()) {
            finalTagExpression = includeExpression;
        } else if (!excludeExpression.isEmpty()) {
            finalTagExpression = "not (" + excludeExpression + ")";
        }

        return finalTagExpression;
    }

    public Mono<ScheduledRunEntity> updateScheduledRun(String id, ScheduledRunRequest request) {
        return scheduledRunRepository.findById(id)
                .flatMap(entity -> {
                    // Cancel existing scheduled task
                    ScheduledFuture<?> task = scheduledTasks.get(id);
                    if (task != null) {
                        task.cancel(false);
                        scheduledTasks.remove(id);
                    }
                    
                    // Update entity with new values
                    entity.setName(request.getName());
                    entity.setIncludeTags(request.getIncludeTags());
                    entity.setExcludeTags(request.getExcludeTags());
                    entity.setCronExpression(request.getCronExpression());
                    
                    return scheduledRunRepository.save(entity);
                })
                .doOnSuccess(savedEntity -> {
                    if (savedEntity.isActive()) {
                        scheduleTask(savedEntity);
                    }
                });
    }

    // Initialize all active scheduled runs on application startup
    public void initializeScheduledRuns() {
        getActiveScheduledRuns()
                .doOnNext(this::scheduleTask)
                .subscribe();
    }
}
