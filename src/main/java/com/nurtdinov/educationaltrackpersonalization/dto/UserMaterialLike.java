package com.nurtdinov.educationaltrackpersonalization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * A synthetic entity for extracting data directly from @JoinTable user_material_like
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserMaterialLike {
    Long materialId;
    String username;
    Long userExternalId;

    List<Long> likedMaterialsList;

    public UserMaterialLike(Long materialId, String username) {
        this.materialId = materialId;
        this.username = username;
    }

    public UserMaterialLike(Long materialId, Long userExternalId) {
        this.materialId = materialId;
        this.userExternalId = userExternalId;
    }

    public UserMaterialLike(String username, Long userExternalId, List<Long> likedMaterialsList) {
        this.username = username;
        this.userExternalId = userExternalId;
        this.likedMaterialsList = likedMaterialsList;
    }

    public UserMaterialLike(Long materialId, String username, Long userExternalId) {
        this.materialId = materialId;
        this.username = username;
        this.userExternalId = userExternalId;
    }
}
