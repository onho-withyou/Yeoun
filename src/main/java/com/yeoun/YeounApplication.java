package com.yeoun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YeounApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeounApplication.class, args);
	}

}
