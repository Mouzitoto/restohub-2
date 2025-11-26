package com.restohub.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestoHubAdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestoHubAdminApiApplication.class, args);
    }
}

