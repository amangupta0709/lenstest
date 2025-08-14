package com.db.lenstest.lensRepository;

import com.db.lenstest.lensDTO.*;
import com.db.lenstest.lensEntity.*;
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

        List<FeatureEntity> features = source.getFeatures().values().stream()
                .map(TestRunEntityMapper::featureToEntity)
                .toList();

        entity.setFeatures(features);
        entity.setTagStats(source.getTagStats());
        entity.setFeatureStats(source.getFeatureStats());
        entity.setScenarioStats(source.getScenarioStats());
        entity.setStepStats(source.getStepStats());
        entity.setRunType(source.getRunType());
        entity.setScheduledRunId(source.getScheduledRunId());
        entity.setProcessId(source.getProcessId());
        entity.setLastHeartbeat(source.getLastHeartbeat());

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

    private static FeatureEntity featureToEntity(Feature feature) {
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setId(feature.getId());
        featureEntity.setName(feature.getName());
        featureEntity.setDescription(feature.getDescription());
        featureEntity.setStatus(feature.getStatus().name());
        featureEntity.setStartedAt(feature.getStartedAt().toString());
        featureEntity.setCompletedAt((feature.getCompletedAt()!=null)?feature.getCompletedAt().toString():null);
        featureEntity.setDuration(feature.getDuration());
        featureEntity.setTags(feature.getTags().stream().toList());

        List<ScenarioEntity> scenarios = feature.getScenarios().values().stream()
                .map(TestRunEntityMapper::scenarioToEntity)
                .toList();

        featureEntity.setScenarios(scenarios);

        return featureEntity;
    }

    private static ScenarioEntity scenarioToEntity(Scenario scenario) {
        ScenarioEntity scenarioEntity = new ScenarioEntity();
        scenarioEntity.setId(scenario.getId().toString());
        scenarioEntity.setName(scenario.getName());
        scenarioEntity.setStatus(scenario.getStatus().name());
        scenarioEntity.setStartedAt(scenario.getStartedAt().toString());
        scenarioEntity.setCompletedAt((scenario.getCompletedAt()!=null)?scenario.getCompletedAt().toString():null);
        scenarioEntity.setDuration(scenario.getDuration());
        scenarioEntity.setTags(scenario.getTags().stream().toList());

        List<StepEntity> steps = scenario.getSteps().stream()
                .map(TestRunEntityMapper::stepToEntity)
                .toList();

        scenarioEntity.setSteps(steps);

        return scenarioEntity;
    }

    private static StepEntity stepToEntity(Step step) {
        StepEntity stepEntity = new StepEntity();
        stepEntity.setId(step.getId().toString());
        stepEntity.setName(step.getName());
        stepEntity.setStatus(step.getStatus().name());
        stepEntity.setStartedAt(step.getStartedAt().toString());
        stepEntity.setCompletedAt((step.getCompletedAt()!=null)?step.getCompletedAt().toString():null);
        stepEntity.setDuration(step.getDuration());
        stepEntity.setError(step.getError());
        stepEntity.setDataTable(step.getDataTable());

        List<LogEntity> logs = step.getLogs().stream()
                .map(TestRunEntityMapper::logToEntity)
                .toList();

        stepEntity.setLogs(logs);

        return stepEntity;
    }

    private static LogEntity logToEntity(Log log){
        LogEntity logEntity = new LogEntity();
        logEntity.setValue(log.getValue());
        logEntity.setShowReport(log.isShowReport());

        return logEntity;
    }
}
