package org.example.lannister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LannisterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LannisterApplication.class, args);
    }

}
