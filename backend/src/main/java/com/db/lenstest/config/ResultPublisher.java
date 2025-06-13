package com.db.lenstest.config;


import com.db.lenstest.domainEntity.TestRunEntity;
import com.db.lenstest.domainRepository.TestRunEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ResultPublisher {

    @Autowired
    private TestRunEntityRepository testRunRepository;

    private static final Sinks.Many<TestRunEntity> sink = Sinks.many().multicast().onBackpressureBuffer();

    public static void publish(TestRunEntity testRunEntity) {
        sink.tryEmitNext(testRunEntity);
    }

    public Flux<TestRunEntity> getTestResultStream() {
        return sink.asFlux();
    }
}
