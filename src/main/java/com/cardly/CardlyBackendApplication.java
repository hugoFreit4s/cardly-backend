package com.cardly;

import com.cardly.config.CorsProperties;
import com.cardly.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@EnableScheduling
public class CardlyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardlyBackendApplication.class, args);
	}

}