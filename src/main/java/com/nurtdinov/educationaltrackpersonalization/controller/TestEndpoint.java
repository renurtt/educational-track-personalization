package com.nurtdinov.educationaltrackpersonalization.controller;

import com.google.common.collect.Lists;
import com.nurtdinov.educationaltrackpersonalization.entity.Course;
import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.TrackStep;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackStepRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class TestEndpoint {

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    LearningMaterialRepository learningMaterialRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TrackRepository trackRepository;

    @Autowired
    TrackStepRepository trackStepRepository;

    @GetMapping("/test")
    public Model121 trye() {
        System.out.println(new Date() + ": request processed");
        return new Model121(restTemplate.getForObject("https://random-word-api.herokuapp.com/word", ArrayList.class).get(0).toString());
    }

    @GetMapping("/learningmaterial")
    public List<LearningMaterial> trye1() {
//        Course course = (Course) learningMaterialRepository.findById(Long.valueOf(278011)).get();
        List<LearningMaterial> course = Lists.newArrayList(learningMaterialRepository.findById(87687L).get());
        course.add(learningMaterialRepository.findById(379699L).get());
        course.add(learningMaterialRepository.findById(351212L).get());
        return course;
    }

    @GetMapping("/test/track")
    public Track trye1(Principal userRequester) {
        Track track = new Track();
        track.setUser(userRepository.findByUsername(userRequester.getName()));
        track.setDestination("finally");
        track.setCreationDate(new Date());

        TrackStep trackStep = new TrackStep();
        trackStep.setLearningMaterial(learningMaterialRepository.findById(379699L).get());
        trackStep.setStepOrderNumber(4L);
        track.setTrackSteps(Collections.singletonList(trackStep));

        trackRepository.save(track);

        return trackRepository.findById(track.getTrackId()).get();
    }

    @GetMapping("/test/track/{id}")
    public Track trye1(Principal userRequester, @PathVariable Long id) {


        return trackRepository.findById(id).get();
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
