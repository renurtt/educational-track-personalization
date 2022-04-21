package com.nurtdinov.educationaltrackpersonalization;

import com.nurtdinov.educationaltrackpersonalization.entity.Article;
import com.nurtdinov.educationaltrackpersonalization.entity.Course;
import com.nurtdinov.educationaltrackpersonalization.entity.Job;
import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.repository.ArticleRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.CourseRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.JobRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.security.ApplicationUserRole;
import com.nurtdinov.educationaltrackpersonalization.security.dto.RegistrationRequest;
import com.nurtdinov.educationaltrackpersonalization.security.service.ApplicationUserService;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses the data from CSVs and inserts into connected DB
 */
@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "application.db.parse-and-insert-data")
@RequiredArgsConstructor
public class DataPrefill implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final JobRepository jobRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ApplicationUserService applicationUserService;


    @Value("${application.db.parse-and-insert-data.override-users}")
    private boolean overrideUsers;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Data prefill is about to start");

        List<Course> courses = readEntitiesFromCsv("courses.csv", Course.class);
        if (!CollectionUtils.isEmpty(courses)) {
            log.info("Overriding courses");
            courseRepository.deleteAll();
            courseRepository.saveAll(courses);
        }

        List<Job> jobs = readEntitiesFromCsv("jobs.csv", Job.class);
        if (!CollectionUtils.isEmpty(jobs)) {
            log.info("Overriding jobs");
            jobRepository.deleteAll();
            jobRepository.saveAll(jobs);
        }

        List<Article> articles = readEntitiesFromCsv("articles.csv", Article.class);
        if (!CollectionUtils.isEmpty(articles)) {
            log.info("Overriding articles");
            articles = articles.stream()
                    .peek(article -> article.setDate(tryParseDate(article.getDateCsv())))
                    .collect(Collectors.toList());

            articleRepository.deleteAll();
            articleRepository.saveAll(articles);
        }

        if (overrideUsers) {
            List<User> users = readEntitiesFromCsv("general.csv", User.class);
            if (!CollectionUtils.isEmpty(users)) {
                log.info("Overriding users");
                users = users.stream()
                        .peek(user -> {
                                    user.setSignUp(tryParseDate(user.getSignUpCsv()));
                                    user.setLastSeen(tryParseDate(user.getLastSeenCsv()));
                                    if (StringUtils.hasText(user.getBirthdayYearCsv()) &&
                                            user.getBirthdayYearCsv().length() == 6) {
                                        user.setBirthdayYear(Long.parseLong(user.getBirthdayYearCsv().substring(0, 4)));
                                    }
                                    user.setUsername(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(3, 11)));

                                    if (StringUtils.hasText(user.getArticlesReadCsv())) {
                                        Set<Article> articlesRead = new HashSet<>();
                                        for (String articleTitle : user.getArticlesReadCsv().split("\n")) {
                                            Article article = articleRepository.findFirstByTitleContains(articleTitle.strip());
                                            if (article != null) {
                                                articlesRead.add(article);
                                            }
                                        }
                                        user.setArticlesRead(articlesRead);
                                    }
                                }
                        )
                        .collect(Collectors.toList());

                userRepository.deleteAll();
                userRepository.saveAll(users);

                for (User user :
                        users) {
                    applicationUserService.doRegister(new RegistrationRequest(user.getUsername(), "123"),
                            Collections.singleton(ApplicationUserRole.USER));
                }
            }
        }


        log.info("Data prefill done");
    }

    private Date tryParseDate(String stringDate) {
        if (!StringUtils.hasText(stringDate)) {
            return null;
        }
        Date date = null;
        try {
            date = DateUtils.parseDate(stringDate,
                    "DD.MM.yyyy HH:mm:ss");
        } catch (ParseException e) {
            log.error("Parse of the following date has failed: " + stringDate);
        }
        return date;
    }

    private static <T> List<T> readEntitiesFromCsv(String fileName, Class<T> entityClass) throws IOException {

        try (FileReader fileReader = new FileReader("../diploma_data_csv/" + fileName)) {
            return new CsvToBeanBuilder(fileReader)
                    .withType(entityClass)
                    .build()
                    .parse();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
