package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class StatisticsDocumentumProjectNewApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatisticsDocumentumProjectNewApplication.class, args);
	}

}
