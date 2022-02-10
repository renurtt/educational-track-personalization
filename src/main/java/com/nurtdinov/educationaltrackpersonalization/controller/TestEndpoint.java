package com.nurtdinov.educationaltrackpersonalization.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class TestEndpoint {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/test")
    public Model121 trye() {
        System.out.println(new Date() + ": request processed");
        return new Model121(restTemplate.getForObject("https://random-word-api.herokuapp.com/word", ArrayList.class).get(0).toString());
    }

    @Data
    @AllArgsConstructor
    public static class Model121 {
        String message;
    }
}
