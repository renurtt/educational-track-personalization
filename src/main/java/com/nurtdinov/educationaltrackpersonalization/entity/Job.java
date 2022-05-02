package com.nurtdinov.educationaltrackpersonalization.entity;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Table(name = "job")
public class Job extends LearningMaterial {
    @CsvBindByName(column = "Название")
    @Column(length=10000)
    String title;

    @CsvBindByName(column = "Детальное описание")
    @Column(length=10000)
    String description;

    @CsvBindByName(column = "Тип вакансии")
    String type;

    @CsvBindByName(column = "Отрасли компании")
    String field;

    @CsvBindByName(column = "Профессия")
    String workLine;

    @CsvBindByName(column = "Опыт работы")
    String workExperience;

    @CsvBindByName(column = "Компания")
    String employer;

    @CsvBindByName(column = "Занятость")
    String occupancy;

    @CsvBindByName(column = "Город")
    String city;
}
