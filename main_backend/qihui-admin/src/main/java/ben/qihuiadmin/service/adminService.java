package ben.qihuiadmin.service;

import ben.qihuiadmin.entity.entity_user.userType;
import ben.qihuiadmin.entity.vo.DataStatsVO;
import ben.qihuiadmin.repository.*;
import org.springframework.stereotype.Service;

@Service
public class adminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ModelRepository modelRepository;
    private final DocumentRepository documentRepository;
    private final ParagraphRepository paragraphRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostFavoriteRepository postFavoriteRepository;
    private final UserBanRepository userBanRepository;
    private final KGRepository kgRepository;

    public adminService(UserRepository userRepository,
                        PostRepository postRepository,
                        CommentRepository commentRepository,
                        ModelRepository modelRepository,
                        DocumentRepository documentRepository,
                        ParagraphRepository paragraphRepository,
                        PostLikeRepository postLikeRepository,
                        CommentLikeRepository commentLikeRepository,
                        PostFavoriteRepository postFavoriteRepository,
                        UserBanRepository userBanRepository,
                        KGRepository kgRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.modelRepository = modelRepository;
        this.documentRepository = documentRepository;
        this.paragraphRepository = paragraphRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.postFavoriteRepository = postFavoriteRepository;
        this.userBanRepository = userBanRepository;
        this.kgRepository = kgRepository;
    }

    public DataStatsVO getStats() {
        long userNum = userRepository.countUsersByType(userType.user);
        long adminNum = userRepository.countUsersByType(userType.admin);
        long bannedNum = userBanRepository.count();
        long postNum = postRepository.count();
        long commentNum = commentRepository.count();
        long favoritesNum = postFavoriteRepository.count();
        long likesNum = postLikeRepository.count() + commentLikeRepository.count();
        long nodeNum = kgRepository.countNodes();
        long relationshipNum = kgRepository.countRelationships();
        long documentNum = documentRepository.count();
        long paragraphNum = paragraphRepository.count();
        long modelNum = modelRepository.count();
        // 暂时由假数据替代
        long dailyVisits = 514;
        long dailyUniqueVisitors = 114;
        return new DataStatsVO(
                new DataStatsVO.UserStats(userNum, bannedNum, adminNum),
                new DataStatsVO.PostStats(postNum, commentNum),
                new DataStatsVO.InteractionStats(favoritesNum, likesNum),
                new DataStatsVO.LearningStats(nodeNum, relationshipNum),
                new DataStatsVO.RagStats(documentNum, paragraphNum),
                new DataStatsVO.ModelStats(modelNum),
                new DataStatsVO.OtherStats(dailyVisits, dailyUniqueVisitors)
        );
    }
}
