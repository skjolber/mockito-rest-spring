# graphql-example
Example application with a simple `@Component` which calls out to an external GraphQL service using HTTP.

Under unit testing, the GraphQL client is pointed to localhost, where a mock endpoint responds with the desired response for each test method.

Init mock as field in unit test.

Configure URL
```
graphql.client.url=http://localhost:${mockitoRestSpringServerPort}/api/graphql
```

in unit test, add
```
@ExtendWith(MockitoEndpointExtension.class)
```

then define unit test field

```
@MockEndpoint(path = "/api")
private MyGraphQLApi myGraphQLApi;
```

Then define mock behavior

```
String response = IOUtils.resourceToString("/responses/response.json", StandardCharsets.UTF_8);
when(myGraphQLApi.request(queryName("getTask"))).thenReturn(response);
```
		
Then trigger the call

```
String title = myService.getTask();
		
// verify result
assertThat(title).isEqualTo("GraphQL docs example");
verify(myGraphQLApi, times(1)).request(anyString());
```


	
