package com.nurtdinov.educationaltrackpersonalization.controller;

import com.google.common.collect.Lists;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
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
    public List<Article> getArticleList() {
        return Lists.newArrayList(articleRepository.findAll());
    }

    @GetMapping("article/{id}")
    public Article getArticleById(@PathVariable Long id) {
        Optional<Article> article = articleRepository.findById(id);
        if (article.isEmpty()) {
            throw new EntityNotFoundException("Nothing found with provided id.");

        }
        return article.get();
    }

    @GetMapping("user/{id}")
    public User getUserById(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException("Nothing found with provided id.");

        }
        return user;
    }
}
