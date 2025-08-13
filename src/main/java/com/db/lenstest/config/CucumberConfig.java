package com.db.lenstest.config;

import com.db.lenstest.LenstestApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest
@ContextConfiguration(classes = LenstestApplication.class)
public class CucumberConfig {
    // Enables Spring dependency injection in step definitions
}
