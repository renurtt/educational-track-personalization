package com.nurtdinov.educationaltrackpersonalization.security.dao;

import com.nurtdinov.educationaltrackpersonalization.security.model.ApplicationUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationUserDAO extends CrudRepository<ApplicationUser, String> {

}

