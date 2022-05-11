package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.TrackStep;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/track")
@Slf4j
public class TrackController {
    @Autowired
    TrackRepository trackRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LearningMaterialRepository learningMaterialRepository;

    @Autowired
    RecommendationService recommendationService;

    @GetMapping("/latest")
    public Track getLatest(Principal userRequester) {
        log.info("POST track/latest/");
        User user = userRepository.findByUsername(userRequester.getName());
        Track track = trackRepository.findFirstByUserOrderByCreationDateDesc(user);
        if (track == null) {
            throw new RestException(HttpStatus.NOT_FOUND, "Track needs to be generated before request.");
        }
        updateCompletion(user, track);
        return track;
    }

    private void updateCompletion(User user, Track track) {
        for (TrackStep trackStep : track.getTrackSteps()) {
            trackStep.setCompleted(user.getMaterialsCompleted().contains(trackStep.getLearningMaterial()));
            learningMaterialRepository.save(trackStep.getLearningMaterial());
        }
    }

    @PostMapping("/generate")
    public void generateNewTrack(Principal userRequester) {
        log.info("POST track/generate/");
        User user = userRepository.findByUsername(userRequester.getName());

        Track track;

        track = recommendationService.generateTrack(user);

        trackRepository.save(track);
    }
}
