package com.db.lenstest.lensRepository;

import com.db.lenstest.lensDTO.Feature;
import com.db.lenstest.lensDTO.Scenario;
import com.db.lenstest.lensDTO.Step;
import com.db.lenstest.lensDTO.TestRun;
import com.db.lenstest.lensEntity.FeatureDto;
import com.db.lenstest.lensEntity.ScenarioDto;
import com.db.lenstest.lensEntity.StepDto;
import com.db.lenstest.lensEntity.TestRunEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TestRunEntityMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static TestRunEntity toEntity(TestRun source) {
        TestRunEntity entity = new TestRunEntity();
        entity.setId(source.getId());
        entity.setStartedAt(source.getStartedAt().toString());
        entity.setCompletedAt((source.getCompletedAt()!=null)?source.getCompletedAt().toString():null);
        entity.setDuration(source.getDuration());
        entity.setExecutionStage(source.getExecutionStage().name());
        entity.setFilterTag(source.getFilterTag());

        List<FeatureDto> features = source.getFeatures().values().stream()
                .map(TestRunEntityMapper::featureToDto)
                .toList();

        entity.setFeatures(features);
        entity.setTagStats(source.getTagStats());
        entity.setFeatureStats(source.getFeatureStats());
        entity.setScenarioStats(source.getScenarioStats());
        entity.setStepStats(source.getStepStats());

        return entity;
    }

//    public static TestRun fromEntity(TestRunEntity entity) {
//        TestRun testRun = new TestRun();
//        testRun.setId(entity.getId());
//        testRun.setStartedAt(entity.getStartedAt());
//        testRun.setCompletedAt(entity.getCompletedAt());
//        testRun.setDuration(entity.getDuration());
//        testRun.setExecutionStage(entity.getExecutionStage());
//        testRun.setFilterTags(entity.getFilterTags());
//
//        try {
//            testRun.setFeatures(mapper.readValue(entity.getFeaturesJson(),
//                    new TypeReference<ConcurrentHashMap<String, Feature>>() {}));
//            testRun.setTagStats(mapper.readValue(entity.getTagStatsJson(),
//                    new TypeReference<ConcurrentHashMap<String, StatusCounter>>() {}));
//            testRun.setFeatureStats(mapper.readValue(entity.getFeatureStatsJson(), StatusCounter.class));
//            testRun.setScenarioStats(mapper.readValue(entity.getScenarioStatsJson(), StatusCounter.class));
//            testRun.setStepStats(mapper.readValue(entity.getStepStatsJson(), StatusCounter.class));
//        } catch (IOException e) {
//            throw new RuntimeException("JSON deserialization failed", e);
//        }
//
//        return testRun;
//    }

    private static FeatureDto featureToDto(Feature feature) {
        FeatureDto dto = new FeatureDto();
        dto.setId(feature.getId());
        dto.setName(feature.getName());
        dto.setDescription(feature.getDescription());
        dto.setStatus(feature.getStatus().name());
        dto.setStartedAt(feature.getStartedAt().toString());
        dto.setCompletedAt((feature.getCompletedAt()!=null)?feature.getCompletedAt().toString():null);
        dto.setDuration(feature.getDuration());
        dto.setTags(feature.getTags().stream().toList());

        List<ScenarioDto> scenarios = feature.getScenarios().values().stream()
                .map(TestRunEntityMapper::scenarioToDto)
                .toList();

        dto.setScenarios(scenarios);
        return dto;
    }

    private static ScenarioDto scenarioToDto(Scenario scenario) {
        ScenarioDto dto = new ScenarioDto();
        dto.setId(scenario.getId().toString());
        dto.setName(scenario.getName());
        dto.setStatus(scenario.getStatus().name());
        dto.setStartedAt(scenario.getStartedAt().toString());
        dto.setCompletedAt((scenario.getCompletedAt()!=null)?scenario.getCompletedAt().toString():null);
        dto.setDuration(scenario.getDuration());
        dto.setTags(scenario.getTags().stream().toList());

        List<StepDto> steps = scenario.getSteps().stream()
                .map(TestRunEntityMapper::stepToDto)
                .toList();

        dto.setSteps(steps);
        return dto;
    }

    private static StepDto stepToDto(Step step) {
        StepDto dto = new StepDto();
        dto.setId(step.getId().toString());
        dto.setName(step.getName());
        dto.setStatus(step.getStatus().name());
        dto.setStartedAt(step.getStartedAt().toString());
        dto.setCompletedAt((step.getCompletedAt()!=null)?step.getCompletedAt().toString():null);
        dto.setDuration(step.getDuration());
        dto.setError(step.getError());
        dto.setLogs(step.getLogs().stream().toList());

        return dto;
    }
}
