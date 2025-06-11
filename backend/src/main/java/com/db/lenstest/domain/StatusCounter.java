package com.db.lenstest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;


@Data
@NoArgsConstructor
public class StatusCounter {

    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicInteger passed = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);
    private final AtomicInteger skipped = new AtomicInteger(0);

    public void increment(TestStatus status) {
        total.incrementAndGet();
        if(status.equals(TestStatus.PASSED)){
            passed.incrementAndGet();
        } else if(status.equals(TestStatus.FAILED)){
            failed.incrementAndGet();
        } else if(status.equals(TestStatus.SKIPPED)){
            skipped.incrementAndGet();
        }
    }

//    public StatusCounter snapshot() {
//        return new StatusCounter(
//                new AtomicInteger(total.get()),
//                new AtomicInteger(passed.get()),
//                new AtomicInteger(failed.get()),
//                new AtomicInteger(skipped.get())
//        );
//    }
}
