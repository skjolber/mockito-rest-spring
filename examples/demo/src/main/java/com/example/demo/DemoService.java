package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;

import org.openapitools.model.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Example of service which calls out to an external service.
 *
 */

@Service
public class DemoService {
	
	@Value("${my.url}")
	private String myUrl;
	
	@Autowired
	private RestTemplate restTemplate;

	public Pet addPet(String name) throws Exception {
		Pet inputPet = new Pet();
		inputPet.setName(name);

		RequestEntity<Pet> request = RequestEntity
			     .post(new URI(myUrl))
			     .accept(MediaType.APPLICATION_JSON)
			     .contentType(MediaType.APPLICATION_JSON)
			     .body(inputPet);

		try {
			ResponseEntity<Pet> responseEntity = restTemplate.exchange(request, Pet.class);
			if(responseEntity.getStatusCode().is2xxSuccessful()) {
				return responseEntity.getBody();
			}
		} catch (RestClientException e) {
			// not able to add
		}
		
		return null;
	}
	
}
