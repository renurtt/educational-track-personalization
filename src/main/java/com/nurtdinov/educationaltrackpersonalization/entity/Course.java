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
@Table(name = "course")
public class Course {
    @CsvBindByName(column = "ID")
    @Id Long id;

    @CsvBindByName(column = "Название")
    @Column(length=10000)
    String title;

    @CsvBindByName(column = "Описание для анонса")
    @Column(length=10000)
    String description;

    @CsvBindByName(column = "Тип")
    String type;

    @CsvBindByName(column = "Внешняя ссылка")
    String externalLink;
}
