//package com.db.lenstest.model;
//
//import com.db.lenstest.dto.ExecutionStatus;
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.time.Instant;
//
//@Data
//@Document(collection = "test_executions")
//public class TestExecution {
//    @Id
//    private String id;
//    private int threadCount;
//    private String testTag;
//    private ExecutionStatus status;
//    private Instant startTime;
//    private Instant endTime;
//    private int totalTests;
//    private int completedTests;
//    private int passedTests;
//    private int failedTests;
//    private String reportPath;
//}
