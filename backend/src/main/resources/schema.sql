CREATE TABLE IF NOT EXISTS build (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    started_at BIGINT,
    completed_at BIGINT,
    duration BIGINT,
    execution_stage TEXT,
    stats_summary_json TEXT,
    tag_summary_json TEXT,
    test_details TEXT
);