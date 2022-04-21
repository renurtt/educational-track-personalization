package com.nurtdinov.educationaltrackpersonalization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "learner_user")
public class User implements Serializable {
    public User(String username) {
        this.username = username;
    }

    @Id
    String username;

    @Unique
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    @CsvBindByName(column = "ID")
    @Column(nullable = false, name = "external_id")
    Long externalId;

    Date lastSeen;

    @CsvBindByName(column = "Последняя авторизация")
    @Transient
    @JsonIgnore
    String lastSeenCsv;

    Date signUp;

    @CsvBindByName(column = "Дата регистрации")
    @Transient
    @JsonIgnore
    String signUpCsv;

    Long birthdayYear;

    @CsvBindByName(column = "Год рождения")
    @Transient
    @JsonIgnore
    String birthdayYearCsv;

    @CsvBindByName(column = "Город проживания")
    String city;

    @CsvBindByName(column = "Желаемая позиция")
    String desiredPosition;

    @CsvBindByName(column = "Вуз")
    String college;

    @CsvBindByName(column = "Прочитанные статьи")
    @Transient
    @JsonIgnore
    String articlesReadCsv;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_article",
            joinColumns = @JoinColumn(
                    name = "user_external_id",
                    referencedColumnName = "external_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "article_id",
                    referencedColumnName = "id"
            )
    )
    private Set<Article> articlesRead = new HashSet<>();

}
