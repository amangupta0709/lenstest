package com.db.lenstest.lensRepository;

import com.db.lenstest.lensEntity.TestRunEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TestRunEntityRepository extends ReactiveMongoRepository<TestRunEntity, Long> {
}
