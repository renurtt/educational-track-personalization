package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.Data;

import java.util.List;

@Data
public class SimilarUsersRequest {
    Double threshold;
    List<UserRecommendationProfileDto> otherUsers;
    UserRecommendationProfileDto targetUser;
}

