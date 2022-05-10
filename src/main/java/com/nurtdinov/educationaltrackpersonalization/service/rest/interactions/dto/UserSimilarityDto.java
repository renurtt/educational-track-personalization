package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class UserSimilarityDto {
    String username;
    Long externalId;
    @EqualsAndHashCode.Exclude
    Double score;
}
