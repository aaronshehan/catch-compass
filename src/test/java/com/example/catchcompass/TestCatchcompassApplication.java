package com.example.catchcompass;

import org.springframework.boot.SpringApplication;

public class TestCatchcompassApplication {

	public static void main(String[] args) {
		SpringApplication.from(CatchcompassApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
