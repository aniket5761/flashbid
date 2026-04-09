package com.example.flashbid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class FlashbidApplication {

	public static void main(String[] args) {
		
		System.out.println(System.getenv("SPRING_DATA_REDIS_URL"));
		SpringApplication.run(FlashbidApplication.class, args);
	}

}
