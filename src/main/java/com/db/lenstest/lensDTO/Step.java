package com.db.lenstest.lensDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Getter
@Setter
public class Step {

    private UUID id;

    private String name;

    private TestStatus status = TestStatus.UNKNOWN;

    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private String duration;

    private String error;

    private ConcurrentLinkedDeque<Log> logs = new ConcurrentLinkedDeque<>();

    private ConcurrentSkipListSet<String> tags = new ConcurrentSkipListSet<>();

    private List<List<String>> dataTable;

//    public void addChild(Feature child) {
//        child.setParentId(this.getId());
//        child.setParent(this);
//        this.tags.addAll(child.getTags());
//        children.add(child);
//    }
//
//    public void complete(Optional<Throwable> error){
//        setCompletedAt(System.currentTimeMillis());
//        setDuration(getCompletedAt()-getStartedAt());
//        error.ifPresent(x -> {
//            setError(readStackTrace(x));
//            setStatus(TestStatus.FAILED);
//        });
//        if (null != parent) {
//            final TestStatus computedStatus = TestStatus.computePriority(getStatus(),parent.getStatus());
//            parent.setStatus(computedStatus);
//            parent.complete(Optional.empty());
//        }
//    }

//    public static String readStackTrace(final Throwable e) {
//        final StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        return sw.toString();
//    }

    //    public String getDurationPretty() {
//        long millis = getDuration();
//        if (1_000L > millis) {
//            return millis + "ms";
//        }
//        if (60_000L > millis) {
//            return String.format("%ds",
//                    TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
//        }
//        if (3_600_000L > millis) {
//            return String.format("%dm %ds",
//                    TimeUnit.MILLISECONDS.toMinutes(millis),
//                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
//        }
//        return String.format("%dh %dm %ds",
//                TimeUnit.MILLISECONDS.toHours(millis),
//                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
//                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
//    }
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

