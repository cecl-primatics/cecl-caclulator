package com.primatics.ignite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
 
@Configuration
public class SpringAppConfig {

    @Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}
