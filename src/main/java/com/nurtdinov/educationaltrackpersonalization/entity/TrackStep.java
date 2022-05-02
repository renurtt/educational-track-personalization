package com.nurtdinov.educationaltrackpersonalization.entity;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.persistence.*;

@Entity
@Data
@Table(name = "track_step")
public class TrackStep {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_step_id_sequence")
    Long trackStepId;

    /**
     * Порядковый номер шага (рекомендации) в текущем треке
     */
    Long stepOrderNumber;

    /**
     * Признак выполненности шага пользователем
     */
    Boolean completed = false;

    /**
     * Признак выполненности шага пользователем
     */
    @ManyToOne
    @JoinColumn(name = "learning_material_id", nullable = false, updatable = false)
    LearningMaterial learningMaterial;
}
