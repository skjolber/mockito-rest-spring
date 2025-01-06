package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

@Component
public class MyGraphQLClient {

	@Value("${graphql.client.url}")
	private String url;

	public String getTask() {
		WebClient webClient = WebClient.create(url);
		WebClientGraphQLClient graphQLClient = MonoGraphQLClient.createWithWebClient(webClient);

		GraphQLResponse response = graphQLClient.reactiveExecuteQuery("query { getTask(id: \"0x3\") { title } }").block();

		return response.extractValueAsObject("getTask.title", String.class);
	}
}