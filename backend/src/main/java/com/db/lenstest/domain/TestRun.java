package com.db.lenstest.domain;

import com.db.lenstest.domainEntity.TestRunEntity;
import com.db.lenstest.domainRepository.TestRunEntityMapper;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Data
public class TestRun {

    private String id;

    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private String duration;

    private ExecutionStage executionStage = ExecutionStage.IN_PROGRESS;

    private StatusCounter featureStats = new StatusCounter();

    private StatusCounter scenarioStats = new StatusCounter();

    private StatusCounter stepStats = new StatusCounter();

    private String filterTag;

    private ConcurrentMap<String, StatusCounter> tagStats = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Feature> features = new ConcurrentHashMap<>();

    public TestRunEntity toEntity(){
        return TestRunEntityMapper.toEntity(this);
    }

    public void addFeature(Feature feature) {
        features.put(feature.getId(), feature);
    }

    public void updateScenarioStats(TestStatus status){
        scenarioStats.getTotal().getAndIncrement();
        if(status.equals(TestStatus.PASSED)){
            scenarioStats.getPassed().incrementAndGet();
        } else if(status.equals(TestStatus.FAILED)){
            scenarioStats.getFailed().incrementAndGet();
        } else if(status.equals(TestStatus.SKIPPED)){
            scenarioStats.getSkipped().incrementAndGet();
        }
    }

    public void updateFeatureStatsAndStatus(Feature feature,TestStatus newStatus){
        if(!feature.getStatus().name().equals(newStatus.name())){
            if(feature.getStatus().equals(TestStatus.PASSED)){
                featureStats.getPassed().decrementAndGet();
            } else if(feature.getStatus().equals(TestStatus.FAILED)){
                featureStats.getFailed().decrementAndGet();
            } else if(feature.getStatus().equals(TestStatus.SKIPPED)){
                featureStats.getSkipped().decrementAndGet();
            }
            if(newStatus.equals(TestStatus.PASSED)){
                featureStats.getPassed().incrementAndGet();
            } else if(newStatus.equals(TestStatus.FAILED)){
                featureStats.getFailed().incrementAndGet();
            } else if(newStatus.equals(TestStatus.SKIPPED)){
                featureStats.getSkipped().incrementAndGet();
            }
            feature.setStatus(newStatus);
        }
    }

    public void updateTagStats(String tag, TestStatus status){
        StatusCounter stats = tagStats.computeIfAbsent(tag, k -> new StatusCounter());
        stats.getTotal().incrementAndGet();
        if(status.equals(TestStatus.PASSED)){
            stats.getPassed().incrementAndGet();
        } else if(status.equals(TestStatus.FAILED)){
            stats.getFailed().incrementAndGet();
        } else if(status.equals(TestStatus.SKIPPED)){
            stats.getSkipped().incrementAndGet();
        }
    }

    public void updateStepStats(TestStatus status){
        stepStats.getTotal().getAndIncrement();
        if(status.equals(TestStatus.PASSED)){
            stepStats.getPassed().incrementAndGet();
        } else if(status.equals(TestStatus.FAILED)){
            stepStats.getFailed().incrementAndGet();
        } else if(status.equals(TestStatus.SKIPPED)){
            stepStats.getSkipped().incrementAndGet();
        }
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
        if (this.startedAt != null && completedAt != null) {
            Duration dur = Duration.between(startedAt, completedAt);
            this.duration = formatDuration(dur);
        }
    }

    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();

        if (millis < 1000) {
            return millis + " ms";
        }

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" min ");
        }
        if (seconds > 0) {
            sb.append(seconds).append(" s");
        }

        return sb.toString().trim();
    }
}

