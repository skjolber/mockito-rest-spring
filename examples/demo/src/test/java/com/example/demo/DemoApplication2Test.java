package com.example.demo;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.skjolber.mockito.rest.spring.MockitoSpringEndpointExtension;
import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

import io.swagger.api.PetApi;
import io.swagger.model.Pet;

/**
 * 
 * Another test to check that there is no invalid state between unit test classes.
 * 
 */

@ExtendWith(MockitoSpringEndpointExtension.class)
@SpringBootTest
public class DemoApplication2Test {

	@Autowired
	private DemoService demoService;
	
	@MockEndpoint
	private PetApi petApi; // this class is generated from an OpenAPI file
	
	@Test
	public void createPetSuccessful() throws Exception {
		// setup mocking
		Pet outputPet = new Pet();
		outputPet.setId(3L);
		outputPet.setName("response2");
		
		ResponseEntity<Pet> entity = ResponseEntity
			.ok()
			.header("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
			.body(outputPet);
		
		when((petApi).addPet(ArgumentMatchers.any(Pet.class))).thenReturn(entity);		
		
		// make the call
		Pet pet = demoService.addPet("request");
		
		// verify result
		assertThat(pet.getName()).isEqualTo(outputPet.getName());
		assertThat(pet.getId()).isEqualTo(3L);

		// verify mock called
		ArgumentCaptor<Pet> argument1 = ArgumentCaptor.forClass(Pet.class);
		verify(petApi, times(1)).addPet(argument1.capture());
		
		assertThat(argument1.getValue().getName()).isEqualTo("request");		
	}

	@Test
	public void createPetFailure() throws Exception {
		// setup mocking
		ResponseEntity<Pet> entity = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		
		when((petApi).addPet(ArgumentMatchers.any(Pet.class))).thenReturn(entity);		
		
		// make the call
		Pet pet = demoService.addPet("request");
		
		// verify result
		assertThat(pet).isNull();

		// verify mock called
		ArgumentCaptor<Pet> argument1 = ArgumentCaptor.forClass(Pet.class);
		verify(petApi, times(1)).addPet(argument1.capture());
		
		assertThat(argument1.getValue().getName()).isEqualTo("request");		
	}

}
