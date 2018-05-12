package com.gemini.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(scanBasePackages ="com.gemini.iot" )
@EnableJpaRepositories(basePackages = {"com.gemini.iot.repository"})
public class MyPlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(MyPlatformApplication.class, args);
	}
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
