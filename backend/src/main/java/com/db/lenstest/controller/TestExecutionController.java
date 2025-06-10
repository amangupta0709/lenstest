package com.db.lenstest.controller;

import com.db.lenstest.service.TestOrchestrator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
public class TestExecutionController {

    private final TestOrchestrator orchestrator;

    public TestExecutionController(TestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/execute")
    public String executeTests(@RequestParam String tag) {
        orchestrator.executeTests(tag);
        return "Tests started for tag: " + tag;
    }
}
