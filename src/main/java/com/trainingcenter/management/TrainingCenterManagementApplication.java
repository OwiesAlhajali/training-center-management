package com.trainingcenter.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainingCenterManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingCenterManagementApplication.class, args);
	}

}