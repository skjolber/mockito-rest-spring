package com.github.skjolber.mockito.rest.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResponseEntityMapper {

	private ObjectMapper mapper; 
	
	public ResponseEntityMapper() {
		this(new ObjectMapper());
	}
	
	public ResponseEntityMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public <T> T unmarshall(String json, Class<T> cls) throws IOException {
		StringReader reader = new StringReader(json);
		
		return mapper.readValue(reader, cls);
	}
	
	public String marshall(Object object, boolean prettyPrint) throws IOException {
		StringWriter writer = new StringWriter();
		if(prettyPrint) {
			mapper.writerWithDefaultPrettyPrinter().writeValue(writer, object);
		} else {
			mapper.writeValue(writer, object);
		}
		return writer.toString();
	}
	
	public ObjectNode toNode(Object object) {
		return mapper.valueToTree(object);
	}

	public String marshall(Object object) throws IOException {
		return marshall(object, false);
	}

	public <T> T unmarshallResource(String path, Class<T> cls) throws IOException {
		File file = getResourceFile(path);
		return unmarshallFile(file, cls);
	}
	
	private File getResourceFile(String path) throws IOException {
		URL directoryURL = getClass().getResource(path);
		if(directoryURL == null) {
			throw new FileNotFoundException(path);
		}
		try {
			return new File(directoryURL.toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public <T> T unmarshallFile(File file, Class<T> cls) throws IOException {
		if(!file.exists()) {
			throw new FileNotFoundException(file.getCanonicalPath());
		}

		FileInputStream in = new FileInputStream(file);
		
		try {
			return (T) mapper.readValue(new InputStreamReader(in, Charset.forName("UTF-8")), cls);
		} finally {
			in.close();
		}
	}
	
	public ObjectMapper getMapper() {
		return mapper;
	}

	public <T> ResponseEntity<T> response(File file, Class<T> cls, Object ... headers) throws IOException {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		if(headers != null) {
			if(headers.length % 2 != 0) {
				throw new IllegalArgumentException("Headers must be in key-value pairs");
			}
			for(int i = 0; i < headers.length; i+=2) {
				if(headers[i+1] instanceof String) {
					responseHeaders.put((String)headers[i], Arrays.asList((String)headers[i+1]));
				} else if(headers[i+1] instanceof List) {
					responseHeaders.put((String)headers[i], (List<String>)headers[i+1]);
				} else throw new IllegalArgumentException("Unexpected value " + headers[i+1]);
			}
		}
		
		T t = unmarshallFile(file, cls);
		
		return new ResponseEntity<T>(t, responseHeaders, HttpStatus.OK);
	}

	public <T> ResponseEntity<T> response(String resource, Class<T> cls, Object ... headers) throws IOException {
		return response(getResourceFile(resource), cls, headers);
	}
}
