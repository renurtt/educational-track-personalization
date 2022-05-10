package com.nurtdinov.educationaltrackpersonalization.service;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.entity.UserSkill;
import com.nurtdinov.educationaltrackpersonalization.exception.EmptyDesiredPostionException;
import com.nurtdinov.educationaltrackpersonalization.exception.EntityNotFoundException;
import com.nurtdinov.educationaltrackpersonalization.exception.NoSkillsException;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserSkillRepository;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.SimilarUsersRequest;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.SimilarUsersResponse;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserSimilarityDto;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserRecommendationProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class SimilarUsersClient {

    private static final String SKILL_LEVEL_DELIMITER = "_";
    private static final double SKILL_SIMILARITY_THRESHOLD = 0.1;
    private static final double DESIRED_POSITION_THRESHOLD = 0.3;

    @Value("${application.recommender.recommenderModelHost}")
    private String recommenderModelHost;

    @Value("${application.recommender.similarUsersInSkillsEndpoint}")
    private String similarUsersInSkillsEndpoint;

    @Value("${application.recommender.similarUsersInDesiredPositionEndpoint}")
    private String similarUsersInDesiredPositionEndpoint;

    @Autowired
    UserSkillRepository userSkillRepository;

    @Autowired
    UserRepository userRepository;

    RestTemplate restTemplate = new RestTemplate();

    /**
     * requests recommendation model for similar in skills users
     * @param userRequester entity of target userr
     * @return similar users (username + id + similarity rate)
     */
    public List<UserSimilarityDto> requestSimilarUsersInSkill(User userRequester) {
        String url = "http://" + recommenderModelHost + "/" + similarUsersInSkillsEndpoint;
        SimilarUsersRequest request = null;
        try {
            request = buildSimilarUsersInSkillRequest(userRequester);
        }
        catch (NoSkillsException e) {
            log.error("Unable to request similar users in skill");
            return Collections.emptyList();
        }

        SimilarUsersResponse similarUsersResponse =
                restTemplate.postForEntity(url, request, SimilarUsersResponse.class).getBody();
        if (similarUsersResponse == null || CollectionUtils.isEmpty(similarUsersResponse.getSimilarUsers())) {
            return null;
        }
        return similarUsersResponse.getSimilarUsers();
    }

    public List<UserSimilarityDto> requestSimilarUsersInDesiredPosition(User userRequester) {
        String url = "http://" + recommenderModelHost + "/" + similarUsersInDesiredPositionEndpoint;
        SimilarUsersRequest request = null;
        try {
            request = buildSimilarUsersInDesiredPositionRequest(userRequester);
        }
        catch (EmptyDesiredPostionException e) {
            log.error("Unable to request similar users in desired position");
            return Collections.emptyList();
        }
        SimilarUsersResponse similarUsersResponse =
                restTemplate.postForEntity(url, request, SimilarUsersResponse.class).getBody();
        if (similarUsersResponse == null || CollectionUtils.isEmpty(similarUsersResponse.getSimilarUsers())) {
            return null;
        }
        return similarUsersResponse.getSimilarUsers();
    }

    private SimilarUsersRequest buildSimilarUsersInDesiredPositionRequest(User userRequester) {
        if (!StringUtils.hasText(userRequester.getDesiredPosition())) {
            throw new EmptyDesiredPostionException();
        }
        SimilarUsersRequest request = new SimilarUsersRequest();
        request.setThreshold(DESIRED_POSITION_THRESHOLD);

        request.setTargetUser(new UserRecommendationProfileDto(userRequester.getUsername(), userRequester.getExternalId(),
                null, userRequester.getDesiredPosition()));

        List<UserRecommendationProfileDto> otherUsersWithDesiredPosition = userRepository.findAllByUsernameNotInAndDesiredPositionIsNotNull(
                Collections.singletonList(userRequester.getUsername()));

        request.setOtherUsers(otherUsersWithDesiredPosition);
        return request;
    }

    private SimilarUsersRequest buildSimilarUsersInSkillRequest(User userRequester) {
        SimilarUsersRequest request = new SimilarUsersRequest();
        request.setThreshold(SKILL_SIMILARITY_THRESHOLD);
        List<UserSkill> targetUserSkillSet = userSkillRepository.findAllByUserUsername(userRequester.getUsername());
        if (CollectionUtils.isEmpty(targetUserSkillSet)) {
            throw new NoSkillsException();
        }
        String targetUserSkillSetString = buildStringSkillSet(SKILL_LEVEL_DELIMITER, targetUserSkillSet);

        request.setTargetUser(new UserRecommendationProfileDto(userRequester.getUsername(), userRequester.getExternalId(),
                targetUserSkillSetString, userRequester.getDesiredPosition()));

        // Other users skills. Step 1. Find all users' skills except current's
        List<UserSkill> otherUserSkills = userSkillRepository.findAllByUserUsernameNotIn(
                Collections.singletonList(userRequester.getUsername()));

        // Other users skills. Step 2. Create a map: User -> Skills list
        Map<User, List<UserSkill>> userToSkillList = new HashMap<>();
        for (UserSkill otherUserSkill : otherUserSkills) {
            if (!userToSkillList.containsKey(otherUserSkill.getUser())) {
                userToSkillList.put(otherUserSkill.getUser(), new ArrayList<>());
            }
            userToSkillList.get(otherUserSkill.getUser()).add(otherUserSkill);
        }

        // Other users skills. Step 3. Turn skill set into a string. Pack up into list of (User, skills string)
        List<UserRecommendationProfileDto> otherUserSkillsDto = new ArrayList<>();
        for (Map.Entry<User, List<UserSkill>> otherUserSkillsListEntry : userToSkillList.entrySet()) {
            otherUserSkillsDto.add(new UserRecommendationProfileDto(
                    otherUserSkillsListEntry.getKey().getUsername(),
                    otherUserSkillsListEntry.getKey().getExternalId(),
                    buildStringSkillSet(SKILL_LEVEL_DELIMITER, otherUserSkillsListEntry.getValue()),
                    otherUserSkillsListEntry.getKey().getDesiredPosition()));
        }

        request.setOtherUsers(otherUserSkillsDto);
        return request;
    }

    private String buildStringSkillSet(String delimiter, List<UserSkill> targetUserSkillSet) {
        StringBuilder result = new StringBuilder();

        for (UserSkill skill : targetUserSkillSet) {
            result.append(skill.getSkill());
            result.append(delimiter);
            result.append(SkillLevelEnum.findByLevel(skill.getLevel()).toString().toLowerCase());
            result.append(" ");
        }
        // delete last space
        result.deleteCharAt(result.length() - 1);

        return result.toString();
    }
}
