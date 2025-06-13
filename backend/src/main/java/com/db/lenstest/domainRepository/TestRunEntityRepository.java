package com.db.lenstest.domainRepository;

import com.db.lenstest.domainEntity.TestRunEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TestRunEntityRepository extends ReactiveMongoRepository<TestRunEntity, Long> {
}
