package com.db.lenstest.lensDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;


@Data
@NoArgsConstructor
public class StatusCounter {
    private AtomicInteger total = new AtomicInteger(0);
    private AtomicInteger passed = new AtomicInteger(0);
    private AtomicInteger failed = new AtomicInteger(0);
    private AtomicInteger skipped = new AtomicInteger(0);
}
