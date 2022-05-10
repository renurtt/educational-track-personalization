package com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarContentForUserResponse {
    List<MatchingMaterialDto> matchingMaterials;
}
