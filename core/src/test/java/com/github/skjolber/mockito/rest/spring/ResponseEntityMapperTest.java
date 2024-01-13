package com.github.skjolber.mockito.rest.spring;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public class ResponseEntityMapperTest {


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
		try {
			mapper.response("/examples/myResponse.json", MyResponse.class, "myHeader", Boolean.TRUE);
			fail();
		} catch(IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void testMapperException2() throws IOException {
		try {
			mapper.response(new File("./src/test/resources/examples/doesnotexist.json"), MyResponse.class, "myHeader");
			fail();
		} catch(FileNotFoundException e) {
			// pass
		}
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
