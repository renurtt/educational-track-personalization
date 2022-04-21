package com.nurtdinov.educationaltrackpersonalization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Persistent;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "article")
public class Article {
    @CsvBindByName(column = "ID")
    @Id
    Long id;

    @CsvBindByName(column = "Название")
    @Column(length = 10000)
    String title;

    @CsvBindByName(column = "Описание для анонса")
    @Column(length = 10000)
    String description;

    @CsvBindByName(column = "Дата изм.")
    @Transient
    @JsonIgnore
    String dateCsv;

    Date date;

    @CsvBindByName(column = "Детальное описание")
    @Column(length = 100000)
    String content;

    @CsvBindByName(column = "Рубрики")
    String category;

    @CsvBindByName(column = "Теги.1")
    String tags;
}
