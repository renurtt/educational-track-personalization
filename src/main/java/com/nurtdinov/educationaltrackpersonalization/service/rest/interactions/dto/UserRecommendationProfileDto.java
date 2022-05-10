package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRecommendationProfileDto {
    String username;
    Long externalId;
    String skillSet;
    String desiredPosition;

    public UserRecommendationProfileDto(String username, Long externalId, String desiredPosition) {
        this.username = username;
        this.externalId = externalId;
        this.desiredPosition = desiredPosition;
    }
}
