package com.ooredoo.turnover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TurnoverApplication {

	public static void main(String[] args) {
		SpringApplication.run(TurnoverApplication.class, args);
	}

}
