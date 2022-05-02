package com.nurtdinov.educationaltrackpersonalization.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "learningMaterialType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Course.class, name = "course"),
        @JsonSubTypes.Type(value = Job.class, name = "job"),
        @JsonSubTypes.Type(value = Article.class, name = "article")
})
public class LearningMaterial {
    @CsvBindByName(column = "ID")
    @Id
    Long id;
}
