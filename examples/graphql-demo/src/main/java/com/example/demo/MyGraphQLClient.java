package com.example.demo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import reactor.core.publisher.Mono;

@Component
public class MyGraphQLClient {

	@Value("${graphql.client.url}")
	private String url;
	
	public String getTask() {
	    //Configure a WebClient for your needs, e.g. including authentication headers and TLS.
		WebClient webClient = WebClient.create(url);
		WebClientGraphQLClient graphQLClient = MonoGraphQLClient.createWithWebClient(webClient);

	    //The GraphQLResponse contains data and errors.
	    GraphQLResponse response = graphQLClient.reactiveExecuteQuery("query { getTask(id: \"0x3\") { title } }").block();

	    return response.extractValueAsObject("getTask.title", String.class);
	}
}