# mockito-rest-spring
This utility supports __unit testing applications which consume external REST services__ defined using Swagger/OpenAPI, RAML or equivalent. 

In a nutshell, mocking external REST services becomes __as simple as mocking any other bean__ using [Mockito].

Users will benefit from

  * full-stack __integration-style unit testing__ - over-the-wire mocking on local ports. 
  * [Mockito] support - i.e. full method/type safety
  * simple setup using JUnit 
    * `@Extension` for [JUnit 5](junit5)
  * Tomcat, Jetty & Undertow support

The REST API must be available either in the form of an annotated interface or a concrete implemenation at compile time. 

When working with OpenAPI definitions this usually means running the code generator two times:

 * once for your regular client (main scope), and
 * once for the to-be-mocked server (test scope).

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is available on central Maven repository.

Example JUnit 5 dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.mockito-rest-spring</groupId>
    <artifactId>junit5-${flavour}</artifactId>
    <version>2.0.x</version>
    <scope>test</scope>
</dependency>
```

where `${flavour}` is `tomcat`, `jetty` or `undertow`.

# Usage
If you prefer skipping to a full example, see [this unit test](examples/demo/src/test/java/com/example/demo/DemoApplication1Test.java). 

# Basics
In your JUnit test, add a `MockitoEndpointExtension` extension:

```java
@ExtendWith(MockitoEndpointExtension.class)
```

before (above) any `SpringExtension` or `@SpringBootTest`. Then mock service endpoints by using

```java
@MockEndpoint(path = "/rest")
private MyRestService myRestService;
```

where the `MyRestService` is either an interface or a concrete `@RestController` implementation. For a custom (or missing) class level [RequestMapping] use

```java
@MockEndpoint(path = "/rest")
private MyRestService myRestService;
```

The returned `serviceMock` instance is a normal [Mockito] mock(..) object. 

### Client configuration
The mock endpoint is started on a random free local port and saved to  `System` property `mockitoRestSpringServerPort`. 

Configure your client to pick up this value, for example via regular properties in Spring:

```
my.server.url=http://localhost:${mockitoRestSpringServerPort}/rest/pet
```

# Details
Create mock response via code

```java
// init response
MyResponse expected = new MyResponse();
expected.setCode(0);
expected.setValue("abc");
```

or from JSON

```java
MyResponse response = jsonUtil.readResource("/example/MyResponse1.xml", MyResponse.class);
```

using your favorite JSON utility. Then mock

```java
when(myRestService.method3(any(MyRequest.class)).thenReturn(expected);
```

or with a `ResponseEntity` wrapper (via OpenAPI `useResponseEntity` parameter):

```java
when(myRestService.method3(any(MyRequest.class)).thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));
```

and apply standard Mockito test approach. 

## Verifing calls
After triggering calls to the mock service, verify number of method calls

```java
ArgumentCaptor<MyRequest> argument1 = ArgumentCaptor.forClass(MyRequest.class);
verify(myRestService, times(1)).method3(argument1.capture());
```

and request details

```java
MyRequest request = argument1.getValue();
assertThat(request.getCode(), is(1));
```

# Alternatives
While this project offers easy-to-setup testing, you might suppliment your testing using the following projects: 

   * [Spring Mock MVC] - using RestTemplate clients
   * [WireMock]

Also, these alternatives do not require the bean/interface being available.

# History

 - 2.0.1: Fix Tomcat temporary folder 
 - 2.0.0: Update to latest Spring, drop JUnit 4 support.
 - 1.0.3: JUnit 5 support for Tomcat, Jetty and Undertow.
 - 1.0.2: Improved JAXB helper, fix artifact id. 
 - 1.0.1: Support for API interfaces, including [Swagger]-generated stubs. See [this unit test](src/test/java/com/github/skjolber/mockito/rest/spring/RestServiceRuleInterfaceTest.java).
 - 1.0.0: Initial version

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:       	https://github.com/skjolber/mockito-rest-spring/issues
[Maven]:                http://maven.apache.org/
[WireMock]:             http://wiremock.org/
[Spring Mock MVC]:      https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html
[Swagger]:				https://github.com/swagger-api/swagger-codegen
[Mockito]:				https://github.com/mockito/mockito
[RequestMapping]:		https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html
