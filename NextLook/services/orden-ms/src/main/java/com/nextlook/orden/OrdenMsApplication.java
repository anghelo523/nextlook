package com.nextlook.orden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrdenMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenMsApplication.class, args);
    }
}
