package com.db.lenstest.domainEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StepDto {
    private String id;

    private String name;

    private String status;

    private String startedAt;

    private String completedAt;

    private String duration;

    private String error;

    private List<String> logs;
}
