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
    
    public String fetchTestExpression() {
        String includeExpression = includeTags != null ? String.join(" or ", includeTags) : "";
        String excludeExpression = excludeTags != null ? String.join(" or ", excludeTags) : "";
        
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
    
    public boolean isValidCronExpression() {
        try {
            CronExpression.parse(this.cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
