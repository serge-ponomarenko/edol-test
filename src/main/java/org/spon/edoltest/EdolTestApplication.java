package org.spon.edoltest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EdolTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdolTestApplication.class, args);
    }

}
