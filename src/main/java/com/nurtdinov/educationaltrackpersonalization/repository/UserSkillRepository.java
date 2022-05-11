package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.UserSkill;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;


public interface UserSkillRepository extends CrudRepository<UserSkill, Long> {
    List<UserSkill> findAllByUserUsername(String username);
    List<UserSkill> findAllByUserUsernameNotIn(Collection<String> userUsername);
    UserSkill findFirstByUserUsernameAndSkill(String username, String skill);
    UserSkill findFirstByUserUsernameAndSkillAndIdNot(String username, String skill, Long id);

    @Override
    @Modifying
    @Query(value="delete from user_skill where id=:id",nativeQuery = true)
    void deleteById(Long id);

    @Query(value="select distinct us.skill from user_skill us",nativeQuery = true)
    List<String> findAllSkillNames();
}
