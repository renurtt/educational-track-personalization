package com.nurtdinov.educationaltrackpersonalization.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class FavoriteCsv {
    @CsvBindByName(column = "User ID")
    String userId;

    @CsvBindByName(column = "Элемент")
    String material;
}
