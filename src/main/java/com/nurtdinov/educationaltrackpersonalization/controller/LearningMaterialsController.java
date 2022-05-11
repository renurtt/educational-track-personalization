package com.nurtdinov.educationaltrackpersonalization.controller;

import com.google.common.collect.Lists;
import com.nurtdinov.educationaltrackpersonalization.dto.ArticleDTO;
import com.nurtdinov.educationaltrackpersonalization.entity.*;
import com.nurtdinov.educationaltrackpersonalization.exception.EntityNotFoundException;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    private final LearningMaterialRepository learningMaterialRepository;

    @GetMapping("course/list")
    public List<Course> getCourseList() {
        return Lists.newArrayList(courseRepository.findAll());
    }

    @GetMapping("course/{id}")
    public Course getCourseById(Principal userRequester, @PathVariable Long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);
        if (courseOptional.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");
        }
        Course course = courseOptional.get();
        if (userRequester != null) {
            User user = userRepository.findByUsername(userRequester.getName());
            course.setLiked(course.getLikedUsers().contains(user));
            course.setCompleted(user.getMaterialsCompleted().contains(course));
        }
        return course;
    }

    @GetMapping("job/list")
    public List<Job> getJobList() {
        return Lists.newArrayList(jobRepository.findAll());
    }

    @GetMapping("job/{id}")
    public Job getJobById(Principal userRequester, @PathVariable Long id) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if (jobOptional.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");
        }
        Job job = jobOptional.get();
        if (userRequester != null) {
            User user = userRepository.findByUsername(userRequester.getName());
            job.setLiked(job.getLikedUsers().contains(user));
            job.setCompleted(user.getMaterialsCompleted().contains(job));
        }
        return job;
    }

    @GetMapping("article/list")
    public List<ArticleDTO> getArticleList(Principal userRequester) {
        ArrayList<Article> articles = Lists.newArrayList(articleRepository.findAll());

        Set<LearningMaterial> readArticles = new HashSet<>();
        if (userRequester != null) {
            User user = userRepository.findByUsername(userRequester.getName());
            readArticles = user.getMaterialsCompleted();
        }

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

        Set<LearningMaterial> readArticles = new HashSet<>();
        if (userRequester != null) {
            User user = userRepository.findByUsername(userRequester.getName());
            readArticles = user.getMaterialsCompleted();
            article.setLiked(article.getLikedUsers().contains(user));
        }

        ArticleDTO articleDTO = mapArticleToDto(article, readArticles);

        return articleDTO;
    }

    @PostMapping("/material/addLike")
    public void addLike(Principal userRequester, @RequestBody LearningMaterial learningMaterialRequest) {
        log.info("POST /material/addLike");

        User user = userRepository.findByUsername(userRequester.getName());

        if (learningMaterialRequest == null || learningMaterialRequest.getId() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "Material id turned out to be null.");
        }

        learningMaterialRepository.addLike(learningMaterialRequest.getId(), user.getUsername());
    }

    @PostMapping("/material/removeLike")
    public void removeLike(Principal userRequester, @RequestBody LearningMaterial learningMaterialRequest) {
        log.info("POST /material/removeLike");

        User user = userRepository.findByUsername(userRequester.getName());

        if (learningMaterialRequest == null || learningMaterialRequest.getId() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "Material id turned out to be null.");
        }

        learningMaterialRepository.removeLike(learningMaterialRequest.getId(), user.getUsername());

    }

    private ArticleDTO mapArticleToDto(Article article, Set<LearningMaterial> readArticles) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setCategory(article.getCategory());
        articleDTO.setDate(article.getDate());
        articleDTO.setDescription(article.getDescription());
        articleDTO.setId(article.getId());
        articleDTO.setContent(article.getContent());
        articleDTO.setTags(article.getTags());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setLiked(article.getLiked());
        articleDTO.setRead(readArticles.stream().anyMatch(x-> Objects.equals(x.getId(), article.getId())));
        return articleDTO;
    }
}
