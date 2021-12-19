package com.delicacy.miniapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.delicacy")
public class MiniappApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniappApplication.class, args);
    }

}