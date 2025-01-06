package com.example.demo;

import static com.github.skjolber.mockito.graphql.matchers.ArgumentMatchers.queryName;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.github.skjolber.mockito.rest.spring.MockitoEndpointExtension;
import com.github.skjolber.mockito.rest.spring.api.MockEndpoint;

@ExtendWith(MockitoEndpointExtension.class)
@SpringBootTest
public class GraphQLTest {

	@Autowired
	private MyGraphQLClient client;
	
	@MockEndpoint(path = "/api")
	private MyGraphQLApi myGraphQLApi;
	
	@Test
	public void getSuccessful() throws Exception {
		// setup mocking
		
		String response = IOUtils.resourceToString("/responses/graphql-response.json", StandardCharsets.UTF_8);
		when(myGraphQLApi.request(queryName("getTask"))).thenReturn(response);
		
		// make the call
		String title = client.getTask();
		
		// verify result
		assertThat(title).isEqualTo("GraphQL docs example");
		verify(myGraphQLApi, times(1)).request(anyString());
	}
	
	@Test
	public void getQueryNotMatched() throws Exception {
		// somewhat crude testing to return null, but this demonstrates the use of matchers
		
		// setup mocking
		String response = IOUtils.resourceToString("/responses/graphql-response.json", StandardCharsets.UTF_8);
		when(myGraphQLApi.request(queryName("getSomeOtherTask"))).thenReturn(response);
		
		// make the call
		try {
			client.getTask();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void getHttpResponseCodes() throws Exception {
		// somewhat crude testing to return null, but this demonstrates the use of matchers
		
		// setup mocking
		when(myGraphQLApi.request(queryName("getTask"))).thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(400)));
		
		// make the call
		try {
			client.getTask();
		} catch(WebClientResponseException e) {
			// pass
			assertEquals(e.getStatusCode(), HttpStatusCode.valueOf(400));
		}
	}

}
