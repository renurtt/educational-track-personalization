package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.UserSkill;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;


public interface UserSkillRepository extends CrudRepository<UserSkill, Long> {
    List<UserSkill> findAllByUserUsername(String username);
    List<UserSkill> findAllByUserUsernameNotIn(Collection<String> userUsername);
}
