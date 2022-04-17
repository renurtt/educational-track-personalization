package com.nurtdinov.educationaltrackpersonalization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    String username;
    String password;
}
