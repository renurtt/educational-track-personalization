package com.nurtdinov.educationaltrackpersonalization.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "track")
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_id_sequence")
    Long trackId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)

    User user;

    Date creationDate;

    String destination;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "track_id")
    List<TrackStep> trackSteps;
}
