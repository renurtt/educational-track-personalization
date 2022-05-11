package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.entity.Article;
import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.EntityNotFoundException;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{username}")
    public User getUserByUsername(Principal userRequester, @PathVariable String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException("Nothing found with provided id.");
        }

        // if the requested user is not current => limited info provided
        if (!userRequester.getName().equals(username)) {
            User userResponse = new User();
            userResponse.setUsername(user.getUsername());
            userResponse.setLastSeen(user.getLastSeen());
            userResponse.setCity(user.getCity());
            userResponse.setFullName(user.getFullName());
            return userResponse;
        }

        return user;
    }

    @GetMapping("/current")
    public User getCurrentUser(Principal userRequester) {
        User user = userRepository.findByUsername(userRequester.getName());

        if (user == null) {
            throw new EntityNotFoundException("Nothing found with provided id.");
        }
        return user;
    }

    @PutMapping("/current")
    public User editCurrentUser(Principal userRequester, @RequestBody User newUser) {
        User user = userRepository.findByUsername(userRequester.getName());

        if (StringUtils.hasText(newUser.getUsername()) && !user.getUsername().equals(newUser.getUsername())) {
            throw new RestException(HttpStatus.FORBIDDEN, "Username is not allowed to be changed.");
        }

        user.setBirthdayYear(newUser.getBirthdayYear());
        user.setCity(newUser.getCity());
        user.setFullName(newUser.getFullName());
        user.setCollege(newUser.getCollege());
        user.setDesiredPosition(newUser.getDesiredPosition());

        user = userRepository.save(user);

        return user;
    }

    @GetMapping("/current/completedArticles")
    public  Set<LearningMaterial> getCompletedArticles(Principal userRequester) {
        User user = userRepository.findByUsername(userRequester.getName());

        Set<LearningMaterial> materials = user.getMaterialsCompleted();

        return materials;
    }

    @PostMapping("/current/addCompletedMaterial")
    public void addArticle(Principal userRequester, @RequestBody LearningMaterial material) {
        User user = userRepository.findByUsername(userRequester.getName());

        if (material == null || material.getId() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "Article id turned out to be null.");
        }
        user.getMaterialsCompleted().add(material);

        userRepository.save(user);
    }

}
