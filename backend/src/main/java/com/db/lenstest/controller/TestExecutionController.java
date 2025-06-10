package com.db.lenstest.controller;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.domain.Test;
import com.db.lenstest.service.TestOrchestrator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class TestExecutionController {

    private final TestOrchestrator orchestrator;

    @Autowired
    private ResultPublisher publisher;

    public TestExecutionController(TestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/execute")
    public String executeTests(@RequestParam String tag) {
        orchestrator.executeTests(tag);
        return "Tests started for tag: " + tag;
    }

    @GetMapping(value = "/results", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTestResults() {
        System.out.println("hahahaha1111");
        return publisher.getTestResultStream();
    }
}
