package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com")
@EnableAsync
public class AttendanceManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceManagementApplication.class, args);
	}
}
