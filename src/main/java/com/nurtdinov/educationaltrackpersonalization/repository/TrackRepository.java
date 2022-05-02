package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.LearningMaterial;
import com.nurtdinov.educationaltrackpersonalization.entity.Track;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface TrackRepository extends CrudRepository<Track, Long> {

    Track findFirstByUserOrderByCreationDateDesc(User user);
}
