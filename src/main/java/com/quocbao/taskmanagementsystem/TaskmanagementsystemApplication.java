package com.quocbao.taskmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.quocbao.taskmanagementsystem")
@EnableScheduling
@EnableAsync
public class TaskmanagementsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskmanagementsystemApplication.class, args);
	}

}
