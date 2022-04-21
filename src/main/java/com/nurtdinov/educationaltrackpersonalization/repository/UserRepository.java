package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Be careful: UserRepository#findById finds by username
 */
public interface UserRepository extends CrudRepository<User, String> {

    User getUserByUsername(String username);
    User findUserByExternalId(Long id);
    User findByUsername(String username);

}
