package com.db.lenstest.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Table("test")
public class Test {

    @Id
    private Long id;

    private Long buildId;
    private Long parentId;
    private String name;

    private TestLevel level; // FEATURE, SCENARIO, STEP
    private TestStatus status = TestStatus.UNKNOWN; // PASSED, FAILED, SKIPPED, UNKNOWN, UNDEFINED

    @Column("started_at")
    private Long startedAt = System.currentTimeMillis();

    @Column("completed_at")
    private Long completedAt;

    private long duration;

    private String error; // only for steps

    @Column("tags_json")
    private String tagsJson;

    @Transient
    @JsonIgnore
    private Set<String> tags = new CopyOnWriteArraySet<>();

    @Column("children_json")
    private String childrenJson;

    @Transient
    @JsonIgnore
    private Queue<Test> children = new ConcurrentLinkedQueue<>();

    @Transient
    @JsonIgnore
    private Test parent;

    public void addChild(Test child) {
        child.setParentId(this.getId());
        child.setParent(this);
        this.tags.addAll(child.getTags());
        children.add(child);
    }

    public void complete(Optional<Throwable> error){
        setCompletedAt(System.currentTimeMillis());
        setDuration(getCompletedAt()-getStartedAt());
        error.ifPresent(x -> {
            setError(readStackTrace(x));
            setStatus(TestStatus.FAILED);
        });
        if (null != parent) {
            final TestStatus computedStatus = TestStatus.computePriority(getStatus(),parent.getStatus());
            parent.setStatus(computedStatus);
            parent.complete(Optional.empty());
        }
    }

    public static String readStackTrace(final Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public String getDurationPretty() {
        long millis = getDuration();
        if (1_000L > millis) {
            return millis + "ms";
        }
        if (60_000L > millis) {
            return String.format("%ds",
                    TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
        }
        if (3_600_000L > millis) {
            return String.format("%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        return String.format("%dh %dm %ds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}

