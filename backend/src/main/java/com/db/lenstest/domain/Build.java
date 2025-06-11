package com.db.lenstest.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Data
@Table("build")
public class Build {

    @Id
    private Long id;
    private String name;

    @Column("started_at")
    private Long startedAt = System.currentTimeMillis();

    @Column("completed_at")
    private Long completedAt;

    private Long duration;

    private ExecutionStage executionStage = ExecutionStage.IN_PROGRESS;

    private String statsSummaryJson;

    @Transient
    private final Map<TestLevel, StatusCounter> stats = new ConcurrentHashMap<>();

    private String tagSummaryJson;

    @Transient
    private final ConcurrentMap<String, TagStats> tagStats = new ConcurrentHashMap<>();

    @Transient
    private List<Test> tests = new ArrayList<>();

    private String testDetails;

    public void updateStats(TestLevel level, TestStatus status) {
        stats.computeIfAbsent(level, k -> new StatusCounter()).increment(status);
    }

    public void updateTagStats(String tag, TestStatus status) {
        tagStats.computeIfAbsent(tag, k -> new TagStats(tag)).getStats().increment(status);
    }

    public String getDurationPretty() {
        long millis = getDuration();
        if (1_000L > millis) {
            return millis + "ms";
        }
        if (60_000L > millis) {
            return String.format("%ds",
                    TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
        }
        if (3_600_000L > millis) {
            return String.format("%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        return String.format("%dh %dm %ds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}

