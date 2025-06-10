package com.db.lenstest.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table
public class Build {
    @Id
    private long id;
    private long startedAt = System.currentTimeMillis();
    private long endedAt;
    @Transient
    private final Queue<RunStats> runStats = new ConcurrentLinkedQueue<>();
    @Transient
    private final Set<TagStats> tagStats = ConcurrentHashMap.newKeySet();
    private Set<Tag> tags = ConcurrentHashMap.newKeySet();
    private ExecutionStage executionStage = ExecutionStage.IN_PROGRESS;
}
