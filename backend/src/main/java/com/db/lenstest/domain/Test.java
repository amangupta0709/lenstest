package com.db.lenstest.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Table
public class Test {

    @Id
    private long id;

    private long buildId;

    private String name;

    private String description;

    private long startedAt = System.currentTimeMillis();

    private long endedAt;

    private long durationMs;

    private String result = Result.PASSED.getResult();

    private String testType = TestType.FEATURE.getType();

    private final Set<Tag> tags = new HashSet<>();

    private String error;

    private Queue<Test> children = new ConcurrentLinkedQueue<>();

    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    private int depth;

    @JsonIgnore
    private Test parent;

    public void addTag(final String tag) {
        if (null != tag && !tag.isBlank()) {
            final Tag t = new Tag(tag);
            this.tags.add(t);
            if (null != parent) {
                parent.addTag(tag);
            }
        }
    }

    public void addChild(Test child) {
        child.setParent(this);
        child.setDepth(depth + 1);
        child.getTags().forEach(tag -> addTag(tag.getName()));
        children.add(child);
    }

    public void complete(Optional<Throwable> error){
        setEndedAt(System.currentTimeMillis());
        error.ifPresent(x -> {
            setError(readStackTrace(x));
            setResult(Result.FAILED.getResult());
        });
        if (null != parent) {
            final Result computedResult = Result.computePriority(Result.valueOf(getResult()),
                    Result.valueOf(parent.getResult()));
            parent.setResult(computedResult.getResult());
            parent.complete(Optional.empty());
        }
    }

    public static String readStackTrace(final Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
