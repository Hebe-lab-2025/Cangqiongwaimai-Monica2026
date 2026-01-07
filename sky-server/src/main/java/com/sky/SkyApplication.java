package com.sky;

import com.apple.eawt.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class SkyApplication {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(SkyApplication.class, args);
    }
}
