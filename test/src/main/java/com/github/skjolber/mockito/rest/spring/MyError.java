package com.github.skjolber.mockito.rest.spring;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyError {
	
    @JsonProperty("value")
    public String value;

    public MyError(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
    
    public void setValue(String value) {
		this.value = value;
	}

}
