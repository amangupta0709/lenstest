package com.db.lenstest.config;


import com.db.lenstest.lensEntity.TestRunEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ResultPublisher {
    private static final Sinks.Many<TestRunEntity> sink = Sinks.many().multicast().onBackpressureBuffer();

    public static void publish(TestRunEntity testRunEntity) {
        sink.tryEmitNext(testRunEntity);
    }

    public Flux<TestRunEntity> getTestResultStream() {
        return sink.asFlux();
    }
}
