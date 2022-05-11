package com.nurtdinov.educationaltrackpersonalization.controller;

import com.nurtdinov.educationaltrackpersonalization.dto.UserSkillDto;
import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.entity.UserSkill;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/skill")
@Slf4j
public class SkillsController {
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserSkillRepository userSkillRepository;
    
    @GetMapping("/userSkillsList")
    public List<UserSkillDto> getCompletedArticles(Principal userRequester) {
        List<UserSkillDto> allSkillsDto = getAllUserSkills(userRequester);
        return allSkillsDto;
    }

    private List<UserSkillDto> getAllUserSkills(Principal userRequester) {
        List<UserSkill> allSkills = userSkillRepository.findAllByUserUsername(userRequester.getName());
        return allSkills.stream().map(this::mapToUserSkillDto).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public void addSkill(Principal userRequester, UserSkillDto userSkillDto) {
        UserSkill userSkill = mapToUserSkill(userSkillDto);
        if (!Objects.equals(userSkill.getUser().getUsername(), userRequester.getName())) {
            throw new RestException(HttpStatus.BAD_REQUEST, "Current user is different from who you're trying to edit");
        }
        userSkillRepository.save(userSkill);
    }



    @PostMapping("/")
    public List<UserSkillDto> updateSkills(Principal userRequester, @RequestBody List<UserSkillDto> userSkillDtos) {
        if (userSkillDtos == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "");
        }
        for (UserSkillDto userSkillDto : userSkillDtos) {
            UserSkill userSkill = mapToUserSkill(userSkillDto);
            if (userSkill.getUser() == null) {
                userSkill.setUser(userRepository.findByUsername(userRequester.getName()));
            }
            if (!StringUtils.hasText(userSkill.getSkill()) || userSkill.getLevel() == null) {
                continue;
            }
            if (userSkill.getId() == null) {
                UserSkill searchBySkill = userSkillRepository.findFirstByUserUsernameAndSkill(userSkill.getUser().getUsername(),
                        userSkill.getSkill());
                if (searchBySkill != null) {
                    userSkill.setId(searchBySkill.getId());
                    userSkill.setLevel(searchBySkill.getLevel());
                }
            } else {
                if (userSkillRepository.findFirstByUserUsernameAndSkillAndIdNot(userSkill.getUser().getUsername(),
                        userSkill.getSkill(), userSkill.getId()) != null) {
                    continue;
                }
            }
            userSkillRepository.save(userSkill);
        }
        return getAllUserSkills(userRequester);
    }

    @DeleteMapping("/remove")
    public List<UserSkillDto> deleteSkill(Principal userRequester, @RequestBody UserSkillDto userSkillDto) {
        userSkillRepository.deleteById(userSkillDto.getId());

        return getAllUserSkills(userRequester);
    }

    @GetMapping("/getSkillNames")
    public List<String> getSkillNames(Principal userRequester) {
        List<String> allSkills = userSkillRepository.findAllSkillNames();
        Collections.sort(allSkills);

        return allSkills;
    }

    private UserSkill mapToUserSkill(UserSkillDto userSkillDto) {
        UserSkill userSkill = new UserSkill();
        userSkill.setSkill(userSkillDto.getSkill());
        userSkill.setLevel(userSkillDto.getLevel());
        userSkill.setId(userSkillDto.getId());
        userSkill.setUser(userRepository.findByUsername(userSkillDto.getUsername()));

        return userSkill;
    }
    private UserSkillDto mapToUserSkillDto(UserSkill userSkill) {
        UserSkillDto userSkillDto = new UserSkillDto();
        userSkillDto.setSkill(userSkill.getSkill());
        userSkillDto.setLevel(userSkill.getLevel());
        userSkillDto.setId(userSkill.getId());
        userSkillDto.setUsername(userSkill.getUser().getUsername());

        return userSkillDto;
    }
}
