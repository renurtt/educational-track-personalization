package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.dto.UserMaterialLike;
import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.TrackStep;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import javax.annotation.Nullable;

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
        return track;
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
