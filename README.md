[![Build Status](https://travis-ci.org/skjolber/mockito-rest-spring.svg?branch=master)](https://travis-ci.org/skjolber/mockito-rest-spring)

# mockito-rest-spring
Spring REST web-service mocking utility which creates real service endpoints on local ports using webserver instances. These endpoints delegate requests directly to mocks.

Users will benefit from

  * full stack client testing
    * interceptors
  * simple JUnit Rule setup

all with the regular advantages of Mockito. Prerequisites:

  * REST web-service bean (API or implementation) must be on test-classpath

While the primary target is Spring-flavored REST, there is really no constraints on using other implementations. 

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is pending release to central Maven repository.

Example dependency config:

```xml
<dependency>
	<groupId>com.github.skjolber</groupId>
	<artifactId>mockito-rest-spring</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	<scope>test</scope>
</dependency>
```

# Usage
If you prefer skipping to a full example, see [this unit test](src/test/java/com/github/skjolber/mockito/rest/spring/RestServiceRuleTest1.java). 

# Basics
In your JUnit test, create a `RestServiceRule`

```java
@Rule
public RestServiceRule rule = RestServiceRule.newInstance();
```

and mock service endpoints by using

```java
MyRestController serviceMock = rule.mock(MyRestController.class, "http://localhost:12345/base/path"); 
```
The returned `serviceMock` instance is a normal Mockito mock(..) object. 

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
when(serviceMock.method3(any(MyRequest.class)).thenReturn(expected);
```

and apply standard Mockito test approach. After triggering calls to the mock service, verify number of method calls

```java
ArgumentCaptor<MyRequest> argument1 = ArgumentCaptor.forClass(MyRequest.class);
verify(serviceMock, times(1)).method3(argument1.capture());
```

and request details

```java
MyRequest request = argument1.getValue();
assertThat(request.getCode(), is(1));
```

# REST service mock as a field
Wrap mock creation using a `@Before` method if you prefer using fields for your mocks:

```java
@Value("${my.service}")
private String address;

private MyRestController serviceMock;

@Before
public void mockService() {
	serviceMock = rule.mock(MyRestController.class, address);
}
```

# Customize mocked endpoint
The Spring context created by the `RestServiceRule` can be customized by passing in a list of `@Configuration` beans.

```java
public RestServiceRule rule = RestServiceRule.newInstance(Arrays.<Class<?>>asList(LoggingSpringWebMvcConfig.class));
```

For example, you might want to add authentication and/or logging, depending on your use-case and client setup.

# Alternatives
While this project offers easy-to-setup testing, alternatives exist which offer more features and somewhat more fine-grained controls: 

   * [Spring Mock MVC] - using RestTemplate clients
   * [WireMock]

Also, these alternatives do not require the web-service bean being available.

# History

 - [1.0.0]: Initial version

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:       	https://github.com/skjolber/mockito-rest-spring/issues
[Maven]:                http://maven.apache.org/
[1.0.0]:		https://github.com/skjolber/mockito-rest-spring/releases/tag/mockito-rest-spring-1.0.0
[WireMock]:             http://wiremock.org/
[Spring Mock MVC]:      http://docs.spring.io/spring-security/site/docs/current/reference/html/test-mockmvc.html


