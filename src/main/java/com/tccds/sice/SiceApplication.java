package com.tccds.sice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiceApplication.class, args);
	}

}
