package com.nurtdinov.educationaltrackpersonalization.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_skill")
public class UserSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "username", referencedColumnName = "username")
    User user;

    String skill;
    Double level;
}
