package com.db.lenstest.config;

import com.db.lenstest.service.ScheduledRunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledRunInitializer {

    @Autowired
    private ScheduledRunService scheduledRunService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduledRuns() {
        log.info("Initializing scheduled runs...");
        scheduledRunService.initializeScheduledRuns();
    }
}
