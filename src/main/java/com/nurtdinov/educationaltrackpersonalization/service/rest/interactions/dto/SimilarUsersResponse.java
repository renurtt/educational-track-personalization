package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.Data;

import java.util.List;

@Data
public class SimilarUsersResponse {
    List<UserSimilarityDto> similarUsers;
}

