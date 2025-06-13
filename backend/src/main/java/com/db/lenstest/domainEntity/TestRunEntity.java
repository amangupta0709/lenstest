package com.db.lenstest.domainEntity;

import com.db.lenstest.domain.StatusCounter;
import lombok.Data;
import org.springframework.data.annotation.Id;

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

    private List<FeatureDto> features;
}
