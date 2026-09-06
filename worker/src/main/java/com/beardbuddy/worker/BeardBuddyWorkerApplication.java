package com.beardbuddy.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.beardbuddy.domain")
@EnableJpaRepositories("com.beardbuddy.repository")
public class BeardBuddyWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeardBuddyWorkerApplication.class, args);
    }
}
