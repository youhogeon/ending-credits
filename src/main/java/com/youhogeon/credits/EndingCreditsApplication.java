package com.youhogeon.credits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EndingCreditsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EndingCreditsApplication.class, args);
	}

	@Bean
	JacksonJsonParser jacksonJsonParser() {
		return new JacksonJsonParser();
	}

}
