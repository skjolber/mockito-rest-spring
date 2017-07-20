package com.github.skjolber.mockito.rest.spring;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponseEntityMapperTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private ResponseEntityMapper mapper = new ResponseEntityMapper();

	@Test
	public void testMapper1() throws IOException {
		mapper.response("/examples/myResponse.json", MyResponse.class, "myHeader", "myValue");		
	}
	
	@Test
	public void testMapper2() throws IOException {
		mapper.response("/examples/myResponse.json", MyResponse.class, "myHeader", Arrays.asList("myValue1", "myValue1"));		
	}

	@Test
	public void testMapperException1() throws IOException {
		exception.expect(IllegalArgumentException.class);
		mapper.response("/examples/myResponse.json", MyResponse.class, "myHeader", Boolean.TRUE);		
	}

	@Test
	public void testMapperException2() throws IOException {
		exception.expect(IllegalArgumentException.class);
		mapper.response(new File("./src/test/resources/examples/doesnotexist.json"), MyResponse.class, "myHeader");		
	}

	@Test
	public void testMarshall1() throws IOException {
		MyResponse response = new MyResponse();
		mapper.marshall(response);
	}

	@Test
	public void testMarshall2() throws IOException {
		MyResponse response = new MyResponse();
		mapper.marshall(response, true);
	}

}
