package com.chrisworks.personal.inventorysystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PropertySource(value= {"classpath:.env"})
public class InventorySystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventorySystemBackendApplication.class, args);
    }

}
