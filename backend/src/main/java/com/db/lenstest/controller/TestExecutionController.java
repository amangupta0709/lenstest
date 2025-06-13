package com.db.lenstest.controller;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.domainEntity.TestRunEntity;
import com.db.lenstest.domainRepository.TestRunEntityRepository;
import com.db.lenstest.service.TestOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class TestExecutionController {

    private final TestOrchestrator orchestrator;

    @Autowired
    private ResultPublisher publisher;

    @Autowired
    private TestRunEntityRepository testRunEntityRepository;

    public TestExecutionController(TestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/execute")
    public String executeTests(@RequestParam String tag) {
        orchestrator.executeTests(tag);
        return "Tests started for tag: " + tag;
    }

    @GetMapping(value = "/results", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TestRunEntity> streamTestResults() {
        return publisher.getTestResultStream();
    }

    @GetMapping("/")
    public List<TestRunEntity> getAllBuilds(){
        return testRunEntityRepository.findAll().collectList().block();
    }
}
