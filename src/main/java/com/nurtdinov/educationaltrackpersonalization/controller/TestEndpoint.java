package com.nurtdinov.educationaltrackpersonalization.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/questionnaire")
    public Model121 questionnaire(@RequestBody UserQuestionnaireData userQuestionnaireData) {
        System.out.println("Name received: " + userQuestionnaireData.getName());
        return new Model121(restTemplate.getForObject("https://random-word-api.herokuapp.com/word", ArrayList.class).get(0).toString());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserQuestionnaireData {
        String name;
    }

    @Data
    @AllArgsConstructor
    public static class Model121 {
        String message;
    }
}
