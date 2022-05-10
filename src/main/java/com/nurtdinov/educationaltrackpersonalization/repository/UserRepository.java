package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserRecommendationProfileDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Be careful: UserRepository#findById finds by username
 */
public interface UserRepository extends CrudRepository<User, String> {

    User getUserByUsername(String username);
    User findUserByExternalId(Long id);
    User findByUsername(String username);

    @Query(value="select lu.external_id from learner_user lu where lu.username=:username",nativeQuery = true)
    Long findExternalIdByUsername(String username);

    @Query(value =
            "select " +
            "new com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserRecommendationProfileDto(username, externalId, desiredPosition) " +
            "from User " +
            "where desiredPosition is not null and username not in :username")
    List<UserRecommendationProfileDto> findAllByUsernameNotInAndDesiredPositionIsNotNull(List<String> username);

}
