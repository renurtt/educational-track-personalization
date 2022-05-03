package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;


public interface LearningMaterialRepository extends CrudRepository<LearningMaterial, Long> {

    @Modifying
    @Transactional
    @Query(value = "insert into user_material_like  (material_id, username) " +
            "values (:material_id, :username)", nativeQuery = true)
    void addLike(@Param("material_id") Long materialId, @Param("username") String username);

    @Modifying
    @Transactional
    @Query(value = "delete from user_material_like where " +
            "material_id = :material_id and username = :username", nativeQuery = true)
    void removeLike(@Param("material_id") Long materialId, @Param("username") String username);

}
