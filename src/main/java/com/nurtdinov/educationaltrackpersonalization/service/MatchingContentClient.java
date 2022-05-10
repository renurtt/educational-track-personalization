package com.nurtdinov.educationaltrackpersonalization.service;

import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.EmptyDesiredPostionException;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchingContentClient {

    @Value("${application.recommender.recommenderModelHost}")
    private String recommenderModelHost;

    @Value("${application.recommender.similarContentForUser}")
    private String similarContentForUserEndpoint;

    @Autowired
    LearningMaterialRepository learningMaterialRepository;

    RestTemplate restTemplate = new RestTemplate();


    public List<MatchingMaterialDto> requestMatchingContent(User userRequester) {
        String url = "http://" + recommenderModelHost + "/" + similarContentForUserEndpoint;
        SimilarContentForUserRequest request;
        try {
            request = buildMatchingContentRequest(userRequester);
        }
        catch (EmptyDesiredPostionException e) {
            log.error("Unable to request matching materials");
            return Collections.emptyList();
        }

        SimilarContentForUserResponse similarMaterialsResponse =
                restTemplate.postForEntity(url, request, SimilarContentForUserResponse.class).getBody();
        if (similarMaterialsResponse == null || CollectionUtils.isEmpty(similarMaterialsResponse.getMatchingMaterials())) {
            return new ArrayList<>();
        }
        return similarMaterialsResponse.getMatchingMaterials();
    }

    private SimilarContentForUserRequest buildMatchingContentRequest(User userRequester) {
        if (!StringUtils.hasText(userRequester.getDesiredPosition())) {
            throw new EmptyDesiredPostionException();
        }

        SimilarContentForUserRequest request = new SimilarContentForUserRequest();
        request.setTargetUser(new SimilarContentUserDto(userRequester.getDesiredPosition().toLowerCase()));
        List<LearningMaterial> allMaterials = (List<LearningMaterial>) learningMaterialRepository.findAll();
        List<SimilarContentMaterialDto> allMaterialsDto = allMaterials
                .stream()
                .map(x -> new SimilarContentMaterialDto(x.getId(), x.getTitle().toLowerCase() + " " + x.getDescription().toLowerCase()))
                .collect(Collectors.toList());
        request.setMaterials(allMaterialsDto);
        return request;
    }
}
