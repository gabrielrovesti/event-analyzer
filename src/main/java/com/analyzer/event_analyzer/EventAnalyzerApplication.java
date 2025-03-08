package com.analyzer.event_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventAnalyzerApplication {
	public static void main(String[] args) {
		SpringApplication.run(EventAnalyzerApplication.class, args);
	}
}
