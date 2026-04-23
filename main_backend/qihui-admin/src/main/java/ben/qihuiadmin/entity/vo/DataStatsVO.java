package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataStatsVO {
    private UserStats userStats;
    private PostStats postStats;
    private InteractionStats interactionStats;
    private LearningStats learningStats;
    private RagStats ragStats;
    private ModelStats modelStats;
    private OtherStats otherStats;

    public DataStatsVO(UserStats userStats,
                       PostStats postStats,
                       InteractionStats interactionStats,
                       LearningStats learningStats,
                       RagStats ragStats,
                       ModelStats modelStats,
                       OtherStats otherStats
                       ) {
        this.userStats = userStats;
        this.postStats = postStats;
        this.interactionStats = interactionStats;
        this.learningStats = learningStats;
        this.ragStats = ragStats;
        this.modelStats = modelStats;
        this.otherStats = otherStats;
    }

    // 内部类

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserStats {
        private long userNum;
        private long bannedNum;
        private long adminNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostStats {
        private long postNum;
        private long commentNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InteractionStats {
        private long favoritesNum;
        private long likesNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearningStats {
        private long nodeNum;
        private long relationshipNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagStats {
        private long documentNum;
        private long paragraphNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModelStats {
        private long modelNum;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OtherStats {
        private long dailyVisits;
        private long dailyUniqueVisitors;
    }
}
