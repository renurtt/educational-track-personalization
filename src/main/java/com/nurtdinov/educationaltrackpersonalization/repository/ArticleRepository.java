package com.nurtdinov.educationaltrackpersonalization.repository;

import com.nurtdinov.educationaltrackpersonalization.entity.Article;
import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article, Long> {
    Article findFirstByTitleContains(String title);
}
