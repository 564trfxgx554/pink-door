package com.pinkdoor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Pink Door 后端启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PinkDoorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinkDoorApplication.class, args);
    }
}
