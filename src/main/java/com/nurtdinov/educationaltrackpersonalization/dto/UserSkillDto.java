package com.nurtdinov.educationaltrackpersonalization.dto;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSkillDto {
    Long id;

    String username;

    String skill;
    Double level;
}
