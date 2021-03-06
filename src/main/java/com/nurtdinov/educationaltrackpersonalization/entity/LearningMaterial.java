package com.nurtdinov.educationaltrackpersonalization.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        defaultImpl = LearningMaterial.class,
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

    @Transient
    @EqualsAndHashCode.Exclude
    Boolean liked;

    @Transient
    String title;

    @Transient
    String description;

    /**
     * The field is NOT being retrieved from DB. Compute it manually.
     */
    @Transient
    @EqualsAndHashCode.Exclude
    Boolean completed;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_material_like",
            joinColumns = @JoinColumn(
                    name = "material_id",
                    referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "username",
                    referencedColumnName = "username"
            )
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<User> likedUsers = new HashSet<>();

    public LearningMaterial(Long id) {
        this.id = id;
    }
}
