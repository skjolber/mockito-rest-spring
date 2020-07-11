# mockito-rest-spring - JUnit 4
Legacy JUnit support for Tomcat, Jetty and Undertow web servers.

# Obtain
Example dependency config:

```xml
<dependency>
    <groupId>com.github.skjolber.mockito-rest-spring</groupId>
    <artifactId>junit4-${flavour}</artifactId>
    <version>1.0.3-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

where ${flavour} is `tomcat`, `jetty` or `undertow`.


# Usage
If you prefer skipping to a full example, see [this unit test](tomcat/src/test/java/com/github/skjolber/mockito/rest/spring/RestServiceRule1Test.java). 

# Basics
In your JUnit test, create a `RestServiceRule`

```java
@Rule
public RestServiceRule rule = RestServiceRule.newInstance();
```

and mock service endpoints by using

```java
MyRestService serviceMock = rule.mock(MyRestService.class, "http://localhost:12345/base/path"); 
```

where the MyRestService is either an interface or a concrete `@RestController` implementation. For a custom (or missing) class level [RequestMapping] use

```java
MyRestService serviceMock = rule.mock(MyRestService.class, "http://localhost:12345/base/path", "/myService"); 
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

private MyRestService serviceMock;

@Before
public void mockService() {
	serviceMock = rule.mock(MyRestService.class, address);
}
```

# Customize mocked endpoint
The Spring context created by the `RestServiceRule` can be customized by passing in a list of `@Configuration` beans.

```java
public RestServiceRule rule = RestServiceRule.newInstance(Arrays.<Class<?>>asList(LoggingSpringWebMvcConfig.class));
```

For example, you might want to add authentication and/or logging, depending on your use-case and client setup.

[Mockito]:				https://github.com/mockito/mockito
