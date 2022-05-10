package com.nurtdinov.educationaltrackpersonalization.service.pojo;

import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRecommendation {
    LearningMaterial learningMaterial;

    @EqualsAndHashCode.Exclude
    double matchingScore;
}
