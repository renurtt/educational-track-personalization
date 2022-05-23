package com.nurtdinov.educationaltrackpersonalization.service;

import com.nurtdinov.educationaltrackpersonalization.dto.UserMaterialLike;
import com.nurtdinov.educationaltrackpersonalization.entity.*;
import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.service.pojo.MaterialRecommendation;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.MatchingMaterialDto;
import com.nurtdinov.educationaltrackpersonalization.service.rest.interactions.dto.UserSimilarityDto;
import org.apache.commons.lang3.RandomUtils;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RecommendationService {

    // Max number of recommendations per track
    private static final int RECOMMENDATIONS_COUNT_MAX = 5;

    @Autowired
    LearningMaterialRepository learningMaterialRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SimilarUsersClient similarUsersClient;

    @Autowired
    MatchingContentClient matchingContentClient;

    public Track generateTrack(User userRequester) {
        List<UserSimilarityDto> usersSimilarInSkills = similarUsersClient.requestSimilarUsersInSkill(userRequester);

        List<UserSimilarityDto> usersSimilarInDesiredPosition = similarUsersClient.requestSimilarUsersInDesiredPosition(userRequester);

        List<RecommendedItem> recommendedItemsOnUserSimilarity = buildRecommendationsOnUserSimilarity(usersSimilarInSkills,
                usersSimilarInDesiredPosition, userRequester);
        // At this point recommendedItemsOnUserSimilarity CONTAINS recommendations for any or both similar users lists OR contains 0 items
        List<MaterialRecommendation> collaborativeFilteringRecommendations = mapRecommendedItemToMaterialRecommendation(recommendedItemsOnUserSimilarity);

        List<MatchingMaterialDto> recommendedMaterialsOnContentMatch = matchingContentClient.requestMatchingContent(userRequester);
        List<MaterialRecommendation> contentBasedRecommendations = mapMatchingMaterialDtoToMaterialRecommendation(recommendedMaterialsOnContentMatch);

        List<MaterialRecommendation> recommendations = buildHybridRecommendations(collaborativeFilteringRecommendations,
                contentBasedRecommendations);


        return buildTrack(userRequester,
                recommendations
                        .stream()
                        .map(MaterialRecommendation::getLearningMaterial)
                        .collect(Collectors.toList()));
    }

    private List<MaterialRecommendation> mapMatchingMaterialDtoToMaterialRecommendation(List<MatchingMaterialDto> recommendedMaterials) {
        List<MaterialRecommendation> materialRecommendations = new ArrayList<>();
        for (MatchingMaterialDto recommendedItem : recommendedMaterials) {
            Optional<LearningMaterial> learningMaterial = learningMaterialRepository.findById(recommendedItem.getMaterialId());
            if (learningMaterial.isEmpty()) {
                continue;
            }
            materialRecommendations.add(new MaterialRecommendation(
                    learningMaterial.get(),
                    recommendedItem.getScore()
            ));
        }
        return materialRecommendations;
    }

    private List<MaterialRecommendation> mapRecommendedItemToMaterialRecommendation(List<RecommendedItem> recommendedItems) {
        List<MaterialRecommendation> materialRecommendations = new ArrayList<>();
        for (RecommendedItem recommendedItem : recommendedItems) {
            Optional<LearningMaterial> learningMaterial = learningMaterialRepository.findById(recommendedItem.getItemID());
            if (learningMaterial.isEmpty()) {
                continue;
            }
            materialRecommendations.add(new MaterialRecommendation(
                    learningMaterial.get(),
                    recommendedItem.getValue()
            ));
        }
        return materialRecommendations;
    }

    /**
     * calculates similar users list if possible and generates recommendations
     *
     * @return recommendations for any or both similar users lists OR  0 items
     */
    private List<RecommendedItem> buildRecommendationsOnUserSimilarity(List<UserSimilarityDto> usersSimilarInSkills,
                                                                       List<UserSimilarityDto> usersSimilarInDesiredPosition,
                                                                       User userRequester) {
        List<RecommendedItem> recommendedItems = new ArrayList<>();
        List<UserSimilarityDto> similarUsers = new ArrayList<>();
        // Step 1. Check for emptiness of lists of both similarities
        if (!CollectionUtils.isEmpty(usersSimilarInSkills) && !CollectionUtils.isEmpty(usersSimilarInDesiredPosition)) {
            // Step 2. If both lists have users then build the intersection
            similarUsers = usersSimilarInDesiredPosition.stream()
                    .distinct()
                    .filter(usersSimilarInSkills::contains)
                    .collect(Collectors.toList());
            List<RecommendedItem> similarUsersIntersectionRecommendations =
                    mahoutRec(userRequester, extractUsernames(similarUsers));

            // Step 3. Check the recommendations for intersection
            if (similarUsersIntersectionRecommendations == null || similarUsersIntersectionRecommendations.size() < RECOMMENDATIONS_COUNT_MAX) {
                // Step 4. If recommendation list for intersection contains < 5 values then build the union of initial users lists
                similarUsers = Stream.concat(usersSimilarInSkills.stream(), usersSimilarInDesiredPosition.stream())
                        .collect(Collectors.toList());

                List<RecommendedItem> similarUsersUnionRecommendations =
                        mahoutRec(userRequester, extractUsernames(similarUsers));
                // Step 5. Output any recommendations for union
                if (similarUsersUnionRecommendations != null) {
                    recommendedItems = similarUsersUnionRecommendations;
                }
            } else {
                // Step 6. If recommendation list for intersection contains >= 5 values then output the recommendations for intersection
                recommendedItems = similarUsersIntersectionRecommendations;
            }
        } else {
            // Step 7. If any of two initial lists is empty then try to find not empty one
            similarUsers = CollectionUtils.isEmpty(usersSimilarInSkills) ? usersSimilarInDesiredPosition : usersSimilarInSkills;
            if (!CollectionUtils.isEmpty(similarUsers)) {
                // Step 8. If at least one of the initial lists is not empty then output recommendations for it
                List<RecommendedItem> similarUsersRecommendations =
                        mahoutRec(userRequester, extractUsernames(similarUsers));
                if (similarUsersRecommendations != null) {
                    recommendedItems = similarUsersRecommendations;
                }
            }
        }
        return recommendedItems;
    }

    private List<MaterialRecommendation> buildHybridRecommendations(List<MaterialRecommendation> collaborativeFilteringRecommendations,
                                                                    List<MaterialRecommendation> contentBasedRecommendations) {
        List<MaterialRecommendation> hybridRecommendations = tryIntersectOrUnite(collaborativeFilteringRecommendations, contentBasedRecommendations);
        if (hybridRecommendations.size() <= RECOMMENDATIONS_COUNT_MAX) {
            return hybridRecommendations;
        }
        List<MaterialRecommendation> hybridRecommendationsPostProcessed = new ArrayList<>();
        List<MaterialRecommendation> jobs = new ArrayList<>();
        List<MaterialRecommendation> articles = new ArrayList<>();
        List<MaterialRecommendation> courses = new ArrayList<>();

        for (MaterialRecommendation recCandidate : hybridRecommendations) {
            if (recCandidate.getLearningMaterial() instanceof Job ) {
                jobs.add(recCandidate);
            } else if (recCandidate.getLearningMaterial() instanceof Article) {
                articles.add(recCandidate);
            } else if (recCandidate.getLearningMaterial() instanceof Course) {
                courses.add(recCandidate);
            }
        }
        if (CollectionUtils.isEmpty(articles) && CollectionUtils.isEmpty(courses)) {
            return hybridRecommendations.subList(0, RECOMMENDATIONS_COUNT_MAX);
        }
        if (CollectionUtils.isEmpty(articles) || CollectionUtils.isEmpty(courses)) {
            List<MaterialRecommendation> nonEmpty = CollectionUtils.isEmpty(articles) ? courses : articles;
            hybridRecommendationsPostProcessed = new ArrayList<>(nonEmpty.subList(0, Math.min(nonEmpty.size(), RECOMMENDATIONS_COUNT_MAX)));
            if (!CollectionUtils.isEmpty(jobs)) {
                if (hybridRecommendationsPostProcessed.size() == RECOMMENDATIONS_COUNT_MAX) {
                    hybridRecommendationsPostProcessed.set(RECOMMENDATIONS_COUNT_MAX-1, jobs.get(0));
                }
                else {
                    hybridRecommendationsPostProcessed.add(jobs.get(0));
                }
            }
            return hybridRecommendationsPostProcessed;
        } else {
            hybridRecommendationsPostProcessed = new ArrayList<>(articles.subList(0, Math.min(articles.size(), RECOMMENDATIONS_COUNT_MAX / 2)));
            hybridRecommendationsPostProcessed.addAll(courses.subList(0, Math.min(courses.size(), RECOMMENDATIONS_COUNT_MAX - hybridRecommendationsPostProcessed.size() - 1)));
            if (!CollectionUtils.isEmpty(jobs)) {
                hybridRecommendationsPostProcessed.add(jobs.get(0));
            }
            for (int i = hybridRecommendationsPostProcessed.size(); i < Math.min(courses.size(), RECOMMENDATIONS_COUNT_MAX); i++) {
                hybridRecommendationsPostProcessed.add(courses.get(i));
            }
            for (int i = hybridRecommendationsPostProcessed.size(); i < Math.min(articles.size(), RECOMMENDATIONS_COUNT_MAX); i++) {
                hybridRecommendationsPostProcessed.add(0, articles.get(i));
            }

            return hybridRecommendationsPostProcessed;
        }
    }

    private List<MaterialRecommendation> tryIntersectOrUnite(List<MaterialRecommendation> collaborativeFilteringRecommendations, List<MaterialRecommendation> contentBasedRecommendations) {
        List<MaterialRecommendation> recommendationsIntersection = new ArrayList<>();
        if (!CollectionUtils.isEmpty(collaborativeFilteringRecommendations) && !CollectionUtils.isEmpty(contentBasedRecommendations)) {
            recommendationsIntersection = contentBasedRecommendations.stream()
                    .distinct()
                    .filter(collaborativeFilteringRecommendations::contains)
                    .collect(Collectors.toList());
            // TODO: may be try to sum the scores for better performance
            if (!CollectionUtils.isEmpty(recommendationsIntersection) && recommendationsIntersection.size() >= RECOMMENDATIONS_COUNT_MAX) {
                return recommendationsIntersection;
            }
        }
        // TODO: may be order by score (if user-based will stop returning 1.0)
        List<MaterialRecommendation> recommendationsConcatenation = Stream.concat(recommendationsIntersection.stream(),
                        Stream.concat(contentBasedRecommendations.stream(), collaborativeFilteringRecommendations.stream()))
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(recommendationsConcatenation)) {
            return recommendationsConcatenation;
        }
        throw new RestException(HttpStatus.NO_CONTENT, "No recommendations were generated.");
    }

    private List<String> extractUsernames(List<UserSimilarityDto> similarUsers) {
        return similarUsers
                .stream()
                .map(UserSimilarityDto::getUsername)
                .collect(Collectors.toList());
    }


    private Track buildTrack(User user, List<LearningMaterial> learningMaterials) {
        Track track = new Track();
        track.setDestination(user.getDesiredPosition());
        track.setUser(user);
        track.setCreationDate(new Date());
        if (learningMaterials == null) {
            track.setTrackSteps(generateTrackSteps());
        } else {
            List<TrackStep> trackSteps = new ArrayList<>();
            for (int i = 0; i < learningMaterials.size(); i++) {
                trackSteps.add(new TrackStep(null,
                        (long) i,
                        false,
                        learningMaterials.get(i)));
            }
            track.setTrackSteps(trackSteps);
        }

        return track;
    }

    private List<TrackStep> generateTrackSteps() {
        List<LearningMaterial> materials = (List<LearningMaterial>) learningMaterialRepository.findAll();

        List<TrackStep> trackSteps = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(2, 6); i++) {
            trackSteps.add(
                    new TrackStep(null,
                            (long) RandomUtils.nextInt(0, 10),
                            false,
                            materials.get(RandomUtils.nextInt(0, materials.size())))
            );
        }
        return trackSteps;
    }

    private List<RecommendedItem> mahoutRec(User userRequester, List<String> sourceUsersUsernames) {
        // Add a current userRequester
        sourceUsersUsernames.add(userRequester.getUsername());

        List<UserMaterialLike> allLikes = learningMaterialRepository.getAllLikesWhereUsernameIn(sourceUsersUsernames);
        FastByIDMap<Collection<Preference>> data = new FastByIDMap<Collection<Preference>>();

        for (UserMaterialLike userMaterialLike :
                allLikes) {
            if (!data.containsKey(userMaterialLike.getUserExternalId())) {
                data.put(userMaterialLike.getUserExternalId(), new ArrayList<>());
            }
            data.get(userMaterialLike.getUserExternalId()).add(new BooleanPreference(
                    userMaterialLike.getUserExternalId(),
                    userMaterialLike.getMaterialId()
            ));
        }
        // if current user has no likes, add him manually
        if (!data.containsKey(userRequester.getExternalId())) {
            data.put(userRequester.getExternalId(), new ArrayList<>());
        }

        FastByIDMap<PreferenceArray> userData = GenericDataModel.toDataMap(data, true);
        GenericDataModel res = new GenericDataModel(userData);
        try {
            CityBlockSimilarity similarity = new CityBlockSimilarity(res);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, res);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(res, neighborhood, similarity);

            // UserID and number of items to be recommended
            List<RecommendedItem> recommended_items = recommender.recommend(userRequester.getExternalId(), RECOMMENDATIONS_COUNT_MAX * 5);

            return recommended_items;
        } catch (Exception ex) {
            System.out.println("An exception occured!");
        }
        return null;
    }

//    private void libRec() {
//        // recommender configuration
//        Configuration conf = new Configuration();
//        Configuration.Resource resource = new Configuration.Resource("rec/cf/userknn-test.properties");
//        conf.addResource(resource);
//
//        // build data model
//        DataModel dataModel = new TextDataModel(conf);
//        dataModel.buildDataModel();
//
//        // set recommendation context
//        RecommenderContext context = new RecommenderContext(conf, dataModel);
//        RecommenderSimilarity similarity = new PCCSimilarity();
//        similarity.buildSimilarityMatrix(dataModel);
//        context.setSimilarity(similarity);
//
//        // training
//        Recommender recommender = new UserKNNRecommender();
//        recommender.recommend(context);
//
//        // evaluation
//        RecommenderEvaluator evaluator = new MAEEvaluator();
//        recommender.evaluate(evaluator);
//
//        // recommendation results
//        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
//        RecommendedFilter filter = new GenericRecommendedFilter();
//        recommendedItemList = filter.filter(recommendedItemList);
//    }

//    private void sparkRec() {
//        // Turn off unnecessary logging
//        Logger.getLogger("org").setLevel(Level.OFF);
//        Logger.getLogger("akka").setLevel(Level.OFF);
//
//        // Create Java spark context
//        SparkConf conf = new SparkConf().setAppName("Collaborative Filtering Example");
//        JavaSparkContext sc = new JavaSparkContext(conf);
//
//        // Read user-item rating file. format - userId,itemId,rating
//        List<UserMaterialLike> userItemRatingsFile1 = learningMaterialRepository.getAllLikes();
//        JavaRDD<UserMaterialLike> userItemRatingsFile = sc.parallelize(userItemRatingsFile1);
//
//
//        // Read item description file. format - itemId, itemName, Other Fields,..
////        JavaRDD<String> itemDescritpionFile = sc.textFile(args[1]);
//        List<LearningMaterial> itemDescritpionFile1 = (List<LearningMaterial>) learningMaterialRepository.findAll();
//        JavaRDD<LearningMaterial> itemDescritpionFile = sc.parallelize(itemDescritpionFile1);
//
//
//        // Map file to Ratings(user,item,rating) tuples
//        JavaRDD<Rating> ratings = userItemRatingsFile.map(new Function<UserMaterialLike, Rating>() {
//            public Rating call(UserMaterialLike like) {
//                return new Rating(like.getUserExternalId().intValue(), like.getMaterialId().intValue(), 1.0);
//            }
//        });
//
//        // Create tuples(itemId,ItemDescription), will be used later to get names of item from itemId
//        JavaPairRDD<Integer,String> itemDescritpion = itemDescritpionFile.mapToPair(
//                new PairFunction<LearningMaterial, Integer, String>() {
//                    @Override
//                    public Tuple2<Integer, String> call(LearningMaterial t) throws Exception {
//                        return new Tuple2<Integer,String>(t.getId().intValue(), t.getId().toString());
//                    }
//                });
//
//        // Build the recommendation model using ALS
//
//        int rank = 10; // 10 latent factors
//        int numIterations = Integer.parseInt("10"); // number of iterations
//
//        MatrixFactorizationModel model = ALS.trainImplicit(JavaRDD.toRDD(ratings),
//                rank, numIterations);
//        //ALS.trainImplicit(arg0, arg1, arg2)
//
//        // Create user-item tuples from ratings
//        JavaRDD<Tuple2<Object, Object>> userProducts = ratings
//                .map(new Function<Rating, Tuple2<Object, Object>>() {
//                    public Tuple2<Object, Object> call(Rating r) {
//                        return new Tuple2<Object, Object>(r.user(), r.product());
//                    }
//                });
//
//        // Calculate the itemIds not rated by a particular user, say user with userId = 1
//        JavaRDD<Integer> notRatedByUser = userProducts.filter(new Function<Tuple2<Object,Object>, Boolean>() {
//            @Override
//            public Boolean call(Tuple2<Object, Object> v1) throws Exception {
//                if (((Integer) v1._1).intValue() != 0) {
//                    return true;
//                }
//                return false;
//            }
//        }).map(new Function<Tuple2<Object,Object>, Integer>() {
//            @Override
//            public Integer call(Tuple2<Object, Object> v1) throws Exception {
//                return (Integer) v1._2;
//            }
//        });
//
//        // Create user-item tuples for the items that are not rated by user, with user id 1
//        JavaRDD<Tuple2<Object, Object>> itemsNotRatedByUser = notRatedByUser
//                .map(new Function<Integer, Tuple2<Object, Object>>() {
//                    public Tuple2<Object, Object> call(Integer r) {
//                        return new Tuple2<Object, Object>(0, r);
//                    }
//                });
//
//        // Predict the ratings of the items not rated by user for the user
//        JavaRDD<Rating> recomondations = model.predict(itemsNotRatedByUser.rdd()).toJavaRDD().distinct();
//
//        // Sort the recommendations by rating in descending order
//        recomondations = recomondations.sortBy(new Function<Rating,Double>(){
//            @Override
//            public Double call(Rating v1) throws Exception {
//                return v1.rating();
//            }
//
//        }, false, 1);
//
//        // Get top 10 recommendations
//        JavaRDD<Rating> topRecomondations = sc.parallelize(recomondations.take(10));
//
//        // Join top 10 recommendations with item descriptions
//        JavaRDD<Tuple2<Rating, String>> recommendedItems = topRecomondations.mapToPair(
//                new PairFunction<Rating, Integer, Rating>() {
//                    @Override
//                    public Tuple2<Integer, Rating> call(Rating t) throws Exception {
//                        return new Tuple2<Integer,Rating>(t.product(),t);
//                    }
//                }).join(itemDescritpion).values();
//
//
//        //Print the top recommendations for user 1.
//        recommendedItems.foreach(new VoidFunction<Tuple2<Rating,String>>() {
//            @Override
//            public void call(Tuple2<Rating, String> t) throws Exception {
//                System.out.println(t._1.product() + "\t" + t._1.rating() + "\t" + t._2);
//            }
//        });
//    }
}
