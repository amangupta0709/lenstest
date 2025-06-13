package com.db.lenstest.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Getter
@Setter
public class Feature {

    private String id;

    private String name;

    private String description;

    private TestStatus status = TestStatus.UNKNOWN;

    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private String duration;

    private ConcurrentSkipListSet<String> tags = new ConcurrentSkipListSet<>();

    private ConcurrentHashMap<UUID,Scenario> scenarios = new ConcurrentHashMap<>();

    public void addScenario(Scenario scenario) {
        scenarios.put(scenario.getId(),scenario);
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

