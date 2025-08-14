package com.db.lenstest.lensRepository;

import com.db.lenstest.lensEntity.TestRunEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TestRunEntityRepository extends ReactiveMongoRepository<TestRunEntity, String> {
    
    // Find runs by execution stage and process ID (for orphan detection)
    Flux<TestRunEntity> findByExecutionStageAndProcessIdNot(String executionStage, String processId);
    
    // Find runs started before a certain time with specific stage
    Flux<TestRunEntity> findByExecutionStageAndStartedAtBefore(String executionStage, LocalDateTime cutoffTime);
}
