package com.db.lenstest.lensEntity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "scheduledRuns")
public class ScheduledRunEntity {
    
    @Id
    private String id;
    
    private String name;
    
    private List<String> includeTags;
    
    private List<String> excludeTags;
    
    private String cronExpression;
    
    private boolean active;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastRunAt;
    
    private String lastRunId;
    
    public ScheduledRunEntity() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
}
