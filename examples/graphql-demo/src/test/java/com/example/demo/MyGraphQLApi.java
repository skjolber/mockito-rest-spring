/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.5.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

public interface MyGraphQLApi {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/graphql",
        produces = { "application/json" },
        consumes = { "application/json"}
    )
    default String request(@Valid @RequestBody String graphQL) throws ResponseStatusException {
    	throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

}
