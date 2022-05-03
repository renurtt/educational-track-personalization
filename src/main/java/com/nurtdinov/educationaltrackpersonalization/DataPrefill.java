package com.nurtdinov.educationaltrackpersonalization;

import com.nurtdinov.educationaltrackpersonalization.dto.FavoriteCsv;
import com.nurtdinov.educationaltrackpersonalization.entity.*;
import com.nurtdinov.educationaltrackpersonalization.repository.*;
import com.nurtdinov.educationaltrackpersonalization.security.ApplicationUserRole;
import com.nurtdinov.educationaltrackpersonalization.security.dto.RegistrationRequest;
import com.nurtdinov.educationaltrackpersonalization.security.service.ApplicationUserService;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final LearningMaterialRepository learningMaterialRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ApplicationUserService applicationUserService;

    @Value("${application.db.parse-and-insert-data.override-users}")
    private boolean overrideUsers;

    private static final boolean updateCoursesToggle;
    private static final boolean updateJobsToggle;
    private static final boolean updateArticlesToggle;
    private static final boolean updateFavoriteToggle;

    static {
        updateCoursesToggle = false;
        updateJobsToggle = false;
        updateArticlesToggle = false;
        updateFavoriteToggle = false;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Data prefill is about to start");

        if (updateCoursesToggle) {
            List<Course> courses = readEntitiesFromCsv("courses.csv", Course.class);
            if (!CollectionUtils.isEmpty(courses)) {
                log.info("Overriding courses");
                courseRepository.deleteAll();
                courseRepository.saveAll(courses);
            }
        }

        if (updateJobsToggle) {
            List<Job> jobs = readEntitiesFromCsv("jobs.csv", Job.class);
            if (!CollectionUtils.isEmpty(jobs)) {
                log.info("Overriding jobs");
                jobRepository.deleteAll();
                jobRepository.saveAll(jobs);
            }
        }

        if (updateArticlesToggle) {
            List<Article> articles = readEntitiesFromCsv("articles.csv", Article.class);
            if (!CollectionUtils.isEmpty(articles)) {
                log.info("Overriding articles");
                articles = articles.stream()
                        .peek(article -> article.setDate(tryParseDate(article.getDateCsv())))
                        .collect(Collectors.toList());

                articleRepository.deleteAll();
                articleRepository.saveAll(articles);
            }
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

        if (updateFavoriteToggle) {
            List<FavoriteCsv> favorites = readEntitiesFromCsv("user_favorites.csv", FavoriteCsv.class);
            if (!CollectionUtils.isEmpty(favorites)) {
                log.info("Overriding favorites");

                for (FavoriteCsv favorite : favorites) {
                    if (!StringUtils.hasText(favorite.getUserId())) {
                        continue;
                    }
                    User user = userRepository.findUserByExternalId(Long.parseLong(favorite.getUserId().substring(0, favorite.getUserId().length() - 2)));
                    if (!StringUtils.hasText(favorite.getMaterial())) {
                        continue;
                    }
                    Long materialId = Long.parseLong(
                            favorite.getMaterial().substring(
                                    favorite.getMaterial().lastIndexOf('[') + 1,
                                    favorite.getMaterial().lastIndexOf(']')
                            )
                    );
                    if (!learningMaterialRepository.existsById(materialId) || user == null) {
                        continue;
                    }
                    try {
                        learningMaterialRepository.addLike(materialId, user.getUsername());
                    } catch (DataIntegrityViolationException ignored) {
                    }
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
