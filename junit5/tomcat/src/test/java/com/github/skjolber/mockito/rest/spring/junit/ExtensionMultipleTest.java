package com.github.skjolber.mockito.rest.spring.junit;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openapitools.api.PetApi;
import org.openapitools.model.Pet;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.github.skjolber.mockito.rest.spring.MockitoEndpointExtension;
import com.github.skjolber.mockito.rest.spring.MyRestController;
import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

/**
 * 
 * Test using a multiple {@link RestController} / generated interfaces.
 * 
 */

@ExtendWith(MockitoEndpointExtension.class)
public class ExtensionMultipleTest {

	private RestTemplate restTemplate = new RestTemplate();

	@MockEndpoint
	private MyRestController serviceMock;

	@MockEndpoint(path = "/rest")
	private PetApi petApi;

	/**
	 * GET method returning an {@linkplain ResponseEntity}.
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testMultipleCalls() throws Exception {
		String message = "abc";
		
		when(serviceMock.method1()).thenReturn(ResponseEntity.ok().body(message));
		
		Pet outputPet = new Pet();
		outputPet.setId(3L);
		outputPet.setName("response");
		
		ResponseEntity<Pet> entity = ResponseEntity
			.ok()
			.header("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
			.body(outputPet);
		
		when((petApi).addPet(ArgumentMatchers.any(Pet.class))).thenReturn(entity);

		// perform call (usually this is what the application under test does)
		// i.e. myEndpointUrl=http://localhost:${mockitoRestSpringServerPort}/some/path
		URI u1 = new URI("http://localhost:" + MockitoEndpointExtension.getPort() + "/rest/pet");
		URI u2 = new URI("http://localhost:" + MockitoEndpointExtension.getPort() + "/rest/method1");
		
		Pet inputPet = new Pet();
		inputPet.setName("input");

		RequestEntity<Pet> request = RequestEntity
			     .post(u1)
			     .accept(MediaType.APPLICATION_JSON)
			     .contentType(MediaType.APPLICATION_JSON)
			     .body(inputPet);

		ResponseEntity<Pet> responseEntity = restTemplate.exchange(request, Pet.class);
		ResponseEntity<String> method1Response = restTemplate.getForEntity(u2, String.class);

		// verify calls

		assertThat(method1Response.getBody()).isEqualTo(message);

		assertThat(responseEntity.getBody().getId()).isEqualTo(outputPet.getId());

		ArgumentCaptor<Pet> argument1 = ArgumentCaptor.forClass(Pet.class);
		verify(petApi, times(1)).addPet(argument1.capture());
		
		assertThat(argument1.getValue().getName()).isEqualTo(inputPet.getName());
	}

}
