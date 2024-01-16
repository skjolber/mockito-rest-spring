package com.example.demo;

import com.github.skjolber.pet.ApiException;
import com.github.skjolber.pet.model.Category;
import com.github.skjolber.pet.model.Pet;
import org.openapitools.client.api.PetApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Example of service which calls out to an external service.
 *
 */

@Service
public class DemoService {

	@Autowired
	private PetApi petApi;

	public Pet addPet(String name) throws ApiException {
		Pet inputPet = new Pet();
		inputPet.setName(name);
		inputPet.setId(3L);
		inputPet.setStatus(Pet.StatusEnum.AVAILABLE);
		Category c = new Category();
		c.setId(1L);
		c.setName("Test");
		inputPet.setCategory(c);
		return petApi.addPet(inputPet);
	}
	
}
