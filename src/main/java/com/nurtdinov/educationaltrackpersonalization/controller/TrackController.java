package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.repository.TrackRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/track")
public class TrackController {
    @Autowired
    TrackRepository trackRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/latest")
    public Track getLatest(Principal userRequester) {
        User user = userRepository.findByUsername(userRequester.getName());
        return trackRepository.findFirstByUserOrderByCreationDateDesc(user);
    }
}
