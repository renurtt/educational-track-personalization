package com.nurtdinov.educationaltrackpersonalization.service;

import com.nurtdinov.educationaltrackpersonalization.dto.UserMaterialLike;
import com.nurtdinov.educationaltrackpersonalization.entity.*;
import com.nurtdinov.educationaltrackpersonalization.repository.LearningMaterialRepository;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {


    @Autowired
    LearningMaterialRepository learningMaterialRepository;

    @Autowired
    SimilarUsersClient similarUsersClient;

    public Track generateTrack(User userRequester) {
        List<UserSimilarityDto> similarUsers = new ArrayList<>();
        List<UserSimilarityDto> usersSimilarInSkills = similarUsersClient.requestSimilarUsersInSkill(userRequester);

        similarUsers.addAll(usersSimilarInSkills);

        List<String> similarUsersUsernames = extractUsernames(similarUsers);

        List<RecommendedItem> recommendedItems = Objects.requireNonNull(
                mahoutRec(userRequester, similarUsersUsernames));

        return generateTrackInternal(userRequester,
                recommendedItems
                        .stream()
                        .map(x -> new LearningMaterial(x.getItemID()))
                        .collect(Collectors.toList()));
    }

    private List<String> extractUsernames(List<UserSimilarityDto> similarUsers) {
        return similarUsers
                .stream()
                .map(UserSimilarityDto::getUsername)
                .collect(Collectors.toList());
    }


    private Track generateTrackInternal(User user, List<LearningMaterial> learningMaterials) {
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

    private List<RecommendedItem> mahoutRec(User user, List<String> sourceUsersUsernames) {
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

        FastByIDMap<PreferenceArray> userData = GenericDataModel.toDataMap(data, true);
        GenericDataModel res = new GenericDataModel(userData);
        try {
            CityBlockSimilarity similarity = new CityBlockSimilarity(res);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, res);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(res, neighborhood, similarity);

            // UserID and number of items to be recommended
            List<RecommendedItem> recommended_items = recommender.recommend(user.getExternalId(), 5);

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
