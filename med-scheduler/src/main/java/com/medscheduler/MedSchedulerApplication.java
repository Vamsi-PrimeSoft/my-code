package com.medscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;  // 👈 import

@SpringBootApplication
@EnableScheduling   // 👈 this enables @Scheduled jobs in your project
public class MedSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedSchedulerApplication.class, args);
    }
}
