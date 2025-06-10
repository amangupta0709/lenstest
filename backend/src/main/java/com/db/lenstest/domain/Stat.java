package com.db.lenstest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Stat {

    protected Set<String> clientId = ConcurrentHashMap.newKeySet();

    private int depth;
    private int total;
    private int passed;
    private int failed;
    private int skipped;
    private long durationMs;

    public Stat(final int depth) {
        this.depth = depth;
    }

//    protected void update(final Test test) {
//        if (clientId.contains(test.t().toString())) {
//            return;
//        }
//        clientId.add(test.getClientId().toString());
//        total++;
//        if (Result.PASSED.getResult().equalsIgnoreCase(test.getResult())) {
//            ++passed;
//        } else if (Result.SKIPPED.getResult().equalsIgnoreCase(test.getResult())) {
//            ++skipped;
//        } else {
//            ++failed;
//        }
//        durationMs += test.getDurationMs();
//    }

    public String getDurationPretty() {
        long millis = getDurationMs();
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
