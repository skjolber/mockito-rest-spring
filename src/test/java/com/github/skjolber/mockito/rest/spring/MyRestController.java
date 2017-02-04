package com.github.skjolber.mockito.rest.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/rest")
public class MyRestController {

	private static Logger logger = LoggerFactory.getLogger(MyRestController.class);

	@RequestMapping(value = "/method1", method = RequestMethod.GET)
	public ResponseEntity<String> method1() {
		logger.info("Method 1");
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("MyResponseHeader", "MyValue");
		return new ResponseEntity<String>("def", responseHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/method2", method = RequestMethod.GET)
	public @ResponseBody MyResponse method2() {
		logger.info("Method 2");
		
		MyResponse response = new MyResponse();
		response.setCode(0);
		response.setValue("def");
		return response;
	}

	@RequestMapping(value = "/method3", method = RequestMethod.POST)
	public @ResponseBody MyResponse method3(@RequestBody MyRequest request) {
		logger.info("Method 3: " + request);
		
		MyResponse response = new MyResponse();
		response.setCode(0);
		response.setValue("def");
		return response;
	}

	@ExceptionHandler(MyException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public @ResponseBody MyError handleException(MyException e) {
	    return new MyError("That doesnt work");
	}

}
