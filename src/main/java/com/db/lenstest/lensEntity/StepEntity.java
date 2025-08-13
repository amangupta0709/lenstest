package com.db.lenstest.lensEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StepEntity {
    private String id;

    private String name;

    private String status;

    private String startedAt;

    private String completedAt;

    private String duration;

    private String error;

    private List<LogEntity> logs;

    private List<List<String>> dataTable;
}
