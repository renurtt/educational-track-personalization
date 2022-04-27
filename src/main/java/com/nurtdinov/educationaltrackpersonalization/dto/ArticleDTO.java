package com.nurtdinov.educationaltrackpersonalization.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nurtdinov.educationaltrackpersonalization.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ArticleDTO extends Article {
    Boolean read;
}
