package com.github.skjolber.mockito.rest.spring;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyRequest {

	@JsonProperty("code")
    public int code;
    
    @JsonProperty("value")
    public String value;

    public String getValue() {
		return value;
	}
    
    public int getCode() {
		return code;
	}
    
    public void setCode(int code) {
		this.code = code;
	}
    
    public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MyRequest [code=" + code + ", value=" + value + "]";
	}
    
}
