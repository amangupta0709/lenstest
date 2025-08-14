package com.db.lenstest.lensDTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.support.CronExpression;

import java.util.List;

@Getter
@Setter
public class ScheduledRunRequest {
    private String name;
    private List<String> includeTags;
    private List<String> excludeTags;
    private String cronExpression;
    
    public boolean isValidCronExpression() {
        try {
            CronExpression.parse(this.cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
