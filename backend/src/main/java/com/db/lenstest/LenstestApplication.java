package com.db.lenstest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.db.lenstest.repository")
public class LenstestApplication {

	public static void main(String[] args) {
		SpringApplication.run(LenstestApplication.class, args);
	}

}
