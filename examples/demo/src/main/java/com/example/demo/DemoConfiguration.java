package com.example.demo;

import com.github.skjolber.pet.ApiClient;
import org.openapitools.client.api.PetApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(DemoConfiguration.class);

	@Value("${my.url}")
	private String myUrl;

	@Bean
	public PetApi client() {
		logger.info("Point client to " + myUrl);
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(myUrl);
		return new PetApi(apiClient);
	}
	
}
