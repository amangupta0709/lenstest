package com.db.lenstest.config;


import com.db.lenstest.domain.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.testng.internal.TestResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ResultPublisher {

    private static final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public static void publishOnScenarioCompletion(Test scenario) {
        System.out.println("--------");
        System.out.println(scenario);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.valueToTree(scenario.getParent());
        sink.tryEmitNext(json.toPrettyString());
    }

    public Flux<String> getTestResultStream() {
        return sink.asFlux()
                .doOnNext(t -> System.out.println("hahahahahah"));
    }
}
