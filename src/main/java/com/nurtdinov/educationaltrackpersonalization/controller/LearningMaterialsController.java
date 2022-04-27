package com.nurtdinov.educationaltrackpersonalization.controller;

import com.google.common.collect.Lists;
import com.nurtdinov.educationaltrackpersonalization.dto.ArticleDTO;
import com.nurtdinov.educationaltrackpersonalization.entity.Article;
import com.nurtdinov.educationaltrackpersonalization.entity.Course;
import com.nurtdinov.educationaltrackpersonalization.entity.Job;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.exception.EntityNotFoundException;
import com.nurtdinov.educationaltrackpersonalization.repository.ArticleRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.CourseRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.JobRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;


@RestController
@RequiredArgsConstructor
@Slf4j
public class LearningMaterialsController {

    private final CourseRepository courseRepository;
    private final JobRepository jobRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @GetMapping("course/list")
    public List<Course> getCourseList() {
        return Lists.newArrayList(courseRepository.findAll());
    }

    @GetMapping("course/{id}")
    public Course getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");

        }
        return course.get();
    }

    @GetMapping("job/list")
    public List<Job> getJobList() {
        return Lists.newArrayList(jobRepository.findAll());
    }

    @GetMapping("job/{id}")
    public Job getJobById(@PathVariable Long id) {
        Optional<Job> job = jobRepository.findById(id);
        if (job.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");

        }
        return job.get();
    }

    @GetMapping("article/list")
    public List<ArticleDTO> getArticleList(Principal userRequester) {
        ArrayList<Article> articles = Lists.newArrayList(articleRepository.findAll());

        Set<Article> readArticles = retrieveReadArticles(userRequester);
        List<ArticleDTO> response = new ArrayList<ArticleDTO>();

        for (Article article : articles) {
            response.add(mapArticleToDto(article, readArticles));
        }

        return response;
    }

    @GetMapping("article/{id}")
    public ArticleDTO getArticleById(Principal userRequester, @PathVariable Long id) {
        log.info("GET /article/" + id);
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");
        }
        Article article = articleOptional.get();

        Set<Article> readArticles = retrieveReadArticles(userRequester);
        ArticleDTO articleDTO = mapArticleToDto(article, readArticles);;

        return articleDTO;
    }

    private Set<Article> retrieveReadArticles(Principal userRequester) {
        Set<Article> readArticles = new HashSet<>();

        if (userRequester != null) {
            User user = userRepository.findByUsername(userRequester.getName());
            readArticles = user.getArticlesRead();
        }
        return readArticles;
    }

    private ArticleDTO mapArticleToDto(Article article, Set<Article> readArticles) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setCategory(article.getCategory());
        articleDTO.setDate(article.getDate());
        articleDTO.setDescription(article.getDescription());
        articleDTO.setId(article.getId());
        articleDTO.setContent(article.getContent());
        articleDTO.setTags(article.getTags());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setRead(readArticles.contains(article));
        return articleDTO;
    }
}
