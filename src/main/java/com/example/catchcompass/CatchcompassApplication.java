package com.example.catchcompass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CatchcompassApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatchcompassApplication.class, args);
	}

}
