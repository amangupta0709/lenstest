package com.db.lenstest.controller;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.lensDTO.ScheduledRunRequest;
import com.db.lenstest.lensDTO.TestRunRequest;
import com.db.lenstest.lensEntity.ScheduledRunEntity;
import com.db.lenstest.lensEntity.TestRunEntity;
import com.db.lenstest.lensRepository.TestRunEntityRepository;
import com.db.lenstest.service.FeatureFilesParser;
import com.db.lenstest.service.ScheduledRunService;
import com.db.lenstest.service.TestOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class TestExecutionController {

    @Autowired
    private TestOrchestrator orchestrator;

    @Autowired
    private ResultPublisher publisher;

    @Autowired
    private TestRunEntityRepository testRunEntityRepository;

    @Autowired
    private ScheduledRunService scheduledRunService;

    private FeatureFilesParser featureFilesParser = new FeatureFilesParser();

    @PostMapping("/execute")
    public String executeTests(@RequestBody TestRunRequest requestBody) {
        String tag = requestBody.fetchTestExpression();
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

    @GetMapping("/tags")
    public Set<String> listTags() {
        return featureFilesParser.getAllTags();
    }

    // Scheduled Run Endpoints
    @PostMapping("/scheduled")
    public Mono<ScheduledRunEntity> createScheduledRun(@RequestBody ScheduledRunRequest request) {
        if (!request.isValidCronExpression()) {
            return Mono.error(new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression()));
        }
        return scheduledRunService.createScheduledRun(request);
    }

    @GetMapping("/scheduled")
    public Flux<ScheduledRunEntity> getAllScheduledRuns() {
        return scheduledRunService.getAllScheduledRuns();
    }

    @GetMapping("/scheduled/active")
    public Flux<ScheduledRunEntity> getActiveScheduledRuns() {
        return scheduledRunService.getActiveScheduledRuns();
    }

    @DeleteMapping("/scheduled/{id}")
    public Mono<Void> deleteScheduledRun(@PathVariable String id) {
        return scheduledRunService.deleteScheduledRun(id);
    }

    @PutMapping("/scheduled/{id}/toggle")
    public Mono<ScheduledRunEntity> toggleScheduledRun(@PathVariable String id, @RequestParam boolean active) {
        return scheduledRunService.toggleScheduledRun(id, active);
    }

    @PutMapping("/scheduled/{id}")
    public Mono<ScheduledRunEntity> updateScheduledRun(@PathVariable String id, @RequestBody ScheduledRunRequest request) {
        if (!request.isValidCronExpression()) {
            return Mono.error(new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression()));
        }
        return scheduledRunService.updateScheduledRun(id, request);
    }
}
