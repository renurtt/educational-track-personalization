package com.nurtdinov.educationaltrackpersonalization.service;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.entity.UserSkill;
import com.nurtdinov.educationaltrackpersonalization.repository.UserSkillRepository;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.SimilarUsersInSkillSetRequest;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.SimilarUsersInSkillSetResponse;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserSimilarityDto;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserRecommendationProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SimilarUsersClient {

    private static final String SKILL_LEVEL_DELIMITER = "_";
    public static final double SKILL_SIMILARITY_THRESHOLD = 0.1;

    @Value("${application.recommender.recommenderModelHost}")
    private String recommenderModelHost;

    @Value("${application.recommender.similarUsersInSkillsEndpoint}")
    private String similarUsersInSkillsEndpoint;

    @Autowired
    UserSkillRepository userSkillRepository;


    RestTemplate restTemplate = new RestTemplate();

    /**
     * requests recommendation model for similar in skills users
     * @param userRequester entity of target userr
     * @return similar users (username + id + similarity rate)
     */
    public List<UserSimilarityDto> requestSimilarUsersInSkill(User userRequester) {
        String url = "http://" + recommenderModelHost + "/" + similarUsersInSkillsEndpoint;
        SimilarUsersInSkillSetRequest request = buildRequest(userRequester);

        SimilarUsersInSkillSetResponse similarUsersInSkillSetResponse =
                restTemplate.postForEntity(url, request, SimilarUsersInSkillSetResponse.class).getBody();
        if (similarUsersInSkillSetResponse == null || CollectionUtils.isEmpty(similarUsersInSkillSetResponse.getSimilarUsers())) {
            return null;
        }
        return similarUsersInSkillSetResponse.getSimilarUsers();
    }

    private SimilarUsersInSkillSetRequest buildRequest(User userRequester) {
        SimilarUsersInSkillSetRequest request = new SimilarUsersInSkillSetRequest();
        request.setThreshold(SKILL_SIMILARITY_THRESHOLD);
        List<UserSkill> targetUserSkillSet = userSkillRepository.findAllByUserUsername(userRequester.getUsername());
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

        // Other users skills. Step 2. Turn skill set into a string. Pack up into list of (User, skills string)
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
