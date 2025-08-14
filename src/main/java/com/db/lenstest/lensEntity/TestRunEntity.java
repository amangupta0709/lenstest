package com.db.lenstest.lensEntity;

import com.db.lenstest.lensDTO.RunType;
import com.db.lenstest.lensDTO.StatusCounter;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TestRunEntity {

    @Id
    private String id;

    private String startedAt;

    private String completedAt;

    private String duration;

    private String executionStage;

    private StatusCounter featureStats;

    private StatusCounter scenarioStats;

    private StatusCounter stepStats;

    private String filterTag;

    private Map<String,StatusCounter> tagStats;

    private List<FeatureEntity> features;

    private RunType runType = RunType.MANUAL;
    
    private String scheduledRunId;
    
    private String processId;
    
    private LocalDateTime lastHeartbeat;
}
