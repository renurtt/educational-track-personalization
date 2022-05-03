package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.TrackStep;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @GetMapping("/latest")
    public Track getLatest(Principal userRequester) {
        log.info("POST track/latest/");
        User user = userRepository.findByUsername(userRequester.getName());
        Track track = trackRepository.findFirstByUserOrderByCreationDateDesc(user);
        if (track == null) {
            throw new RestException(HttpStatus.NOT_FOUND, "Track needs to be generated before request.");
        }
        return track;
    }

    @PostMapping("/generate")
    public void generateNewTrack(Principal userRequester) {
        log.info("POST track/generate/");
        User user = userRepository.findByUsername(userRequester.getName());

        List<LearningMaterial> materials = (List<LearningMaterial>) learningMaterialRepository.findAll();

        Track track = new Track();
        track.setDestination(user.getDesiredPosition());
        track.setUser(user);
        track.setCreationDate(new Date());
        track.setTrackSteps(new ArrayList<>());
        for (int i = 0; i < RandomUtils.nextInt(2, 6); i++) {
            track.getTrackSteps().add(
                    new TrackStep(null,
                            (long) RandomUtils.nextInt(0, 10),
                            false,
                            materials.get(RandomUtils.nextInt(0, materials.size())))
            );
        }
        trackRepository.save(track);
    }
}
