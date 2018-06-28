[![Build Status](https://travis-ci.org/skjolber/mockito-rest-spring.svg?branch=master)](https://travis-ci.org/skjolber/mockito-rest-spring)

# mockito-rest-spring
Spring REST web-service mocking utility which creates real service endpoints on local ports using webserver instances. These endpoints delegate requests directly to mocks.

Users will benefit from

  * full stack client testing
    * interceptors
  * simple setup
  * [JUnit 4](junit4) & [JUnit 5](junit5) support
  * Tomcat, Jetty & Undertow support

all with the regular advantages of [Mockito]. The REST API must be available either in the form
of an annotated interface or a concrete implemenation.

While the primary target is Spring-flavored REST, there is really no constraints on using other implementations. 

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is available on central Maven repository.

Example JUnit 5 dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.mockito-rest-spring</groupId>
    <artifactId>junit5-tomcat</artifactId>
    <version>1.0.3</version>
    <scope>test</scope>
</dependency>
```

or for JUnit 4

```xml
<dependency>
    <groupId>com.github.skjolber-mockito-rest-spring</groupId>
    <artifactId>junit4-tomcat</artifactId>
    <version>1.0.3</version>
    <scope>test</scope>
</dependency>
```

# Usage
The below is for JUnit 5. For JUnit 4 go [here](junit4).

If you prefer skipping to a full example, see [this unit test](junit5/core-junit5/src/test/java/com/github/skjolber/mockito/rest/spring/ExtensionMultipleTest.java). 

# Basics
In your JUnit test, add a `MockitoEndpointExtension` extension:

```java
@ExtendWith(MockitoEndpointExtension.class)
```

and mock service endpoints by using

```java
@MockEndpoint(path = "/rest")
private MyRestService myRestService;
```

where the MyRestService is either an interface or a concrete `@RestController` implementation. For a custom (or missing) class level [RequestMapping] use

```java
@MockEndpoint(path = "/rest")
private MyRestService myRestService;
```

The returned `serviceMock` instance is a normal [Mockito] mock(..) object. 

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

and apply standard Mockito test approach. After triggering calls to the mock service, verify number of method calls

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
While this project offers easy-to-setup testing, alternatives exist which offer more features and somewhat more fine-grained controls: 

   * [Spring Mock MVC] - using RestTemplate clients
   * [WireMock]

Also, these alternatives do not require the bean/interface being available.

# History

 - [1.0.3]: JUnit 5 support for Tomcat, Jetty and Undertow.
 - 1.0.2: Improved JAXB helper, fix artifact id. 
 - 1.0.1: Support for API interfaces, including [Swagger]-generated stubs. See [this unit test](src/test/java/com/github/skjolber/mockito/rest/spring/RestServiceRuleInterfaceTest.java).
 - 1.0.0: Initial version

[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:       	https://github.com/skjolber/mockito-rest-spring/issues
[Maven]:                http://maven.apache.org/
[1.0.3]:				https://github.com/skjolber/mockito-rest-spring/releases/tag/mockito-spring-rest-1.0.3
[WireMock]:             http://wiremock.org/
[Spring Mock MVC]:      http://docs.spring.io/spring-security/site/docs/current/reference/html/test-mockmvc.html
[Swagger]:				https://github.com/swagger-api/swagger-codegen
[Mockito]:				https://github.com/mockito/mockito
[RequestMapping]:		https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html
