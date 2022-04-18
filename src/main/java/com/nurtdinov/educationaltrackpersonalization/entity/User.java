package com.nurtdinov.educationaltrackpersonalization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "learner_user")
public class User {
    public User(String username) {
        this.username = username;
    }

    @Id String username;
}
