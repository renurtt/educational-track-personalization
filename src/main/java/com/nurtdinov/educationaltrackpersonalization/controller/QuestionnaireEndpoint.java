package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@RestController
public class QuestionnaireEndpoint {

    private final RestTemplate restTemplate = new RestTemplate();

    final UserRepository userRepository;

    public QuestionnaireEndpoint(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/questionnaire")
    public TestEndpoint.Model121 questionnaire(@RequestBody TestEndpoint.UserQuestionnaireData userQuestionnaireData) {
        System.out.println("Name received: " + userQuestionnaireData.getName());
        User user = new User();
        user.setUsername(userQuestionnaireData.getName());
        SecurityContextHolder.getContext();
        userRepository.save(user);

        return new TestEndpoint.Model121(restTemplate.getForObject("https://random-word-api.herokuapp.com/word", ArrayList.class).get(0).toString() + user.getUsername());
    }
}
