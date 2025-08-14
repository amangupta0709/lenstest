package com.db.lenstest.lensRepository;

import com.db.lenstest.lensEntity.ScheduledRunEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ScheduledRunRepository extends ReactiveMongoRepository<ScheduledRunEntity, String> {
    Flux<ScheduledRunEntity> findByActiveTrue();
}
