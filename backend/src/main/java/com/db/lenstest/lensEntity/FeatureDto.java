package com.db.lenstest.lensEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FeatureDto {
    private String id;

    private String name;

    private String description;

    private String status;

    private String startedAt;

    private String completedAt;

    private String duration;

    private List<String> tags;

    private List<ScenarioDto> scenarios;
}
