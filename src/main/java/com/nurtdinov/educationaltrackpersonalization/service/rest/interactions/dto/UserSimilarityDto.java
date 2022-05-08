package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.Data;

@Data
public class UserSimilarityDto {
    String username;
    Long externalId;
    Double score;
}
