package ben.qihuiai.controller;

import ben.qihuiai.entity.RestBean;
import ben.qihuiai.entity.dto.LearningInfoDto;
import ben.qihuiai.entity.dto.QuizResultListDto;
import ben.qihuiai.entity.vo.LearningPathVO;
import ben.qihuiai.entity.vo.QuizVO;
import ben.qihuiai.entity.vo.cltVO;
import ben.qihuiai.service.LPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learn")
public class LPController {
    private final LPService lpService;
    public LPController(LPService lpService) {
        this.lpService = lpService;
    }

    // 判断用户是否做过能力测试
    @GetMapping("/judge/learning_path")
    public ResponseEntity<?> judgeUser(@RequestParam(name = "userId") long userId) {
        String res = lpService.getUserGraphAndBKT(userId);
        return ResponseEntity.ok(RestBean.successType7("判断完成", res));
    }

    // 获取用户已有的学习路线
    @GetMapping("/gain/learning_path")
    public ResponseEntity<?> gainLearningPath(@RequestParam(name = "userId") long userId) {
        LearningPathVO vo = lpService.gainLearningPath(userId);
        return ResponseEntity.ok(RestBean.successType1("获取用户已有的学习路线成功", vo));
    }

    // 获取可选择的学习目标的列表
    @GetMapping("/quiz/choose_learning_target")
    public ResponseEntity<?> chooseLearningTarget() {
        cltVO vo = lpService.getLearningTarget();
        return ResponseEntity.ok(RestBean.successType1("获取可选学习目标列表成功", vo));
    }

    // 获取定制学习路线用的考察题目
    @PostMapping("/quiz/learning_path")
    public ResponseEntity<?> generateQuiz(@RequestBody LearningInfoDto dto) {
        if (dto.getLearningTarget() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("学习目标未填写"));
        }
        List<QuizVO> vos = lpService.getQuiz(dto);
        return ResponseEntity.ok(RestBean.successType1("获取测试题目成功", vos));
    }

    // 学习路线规划
    @PostMapping("/generate/learning_path")
    public ResponseEntity<?> generateLearningPath(@RequestBody QuizResultListDto dto) {
        if (dto.getTarget() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("学习目标不能为空"));
        }
        else if (dto.getTend() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("用户倾向的学习路线不能为空"));
        }
        else if (dto.getQuizResults() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("答题得分情况不能为空"));
        }
        LearningPathVO vo = lpService.generateLearningPath(dto);
        return ResponseEntity.ok(RestBean.successType1("学习路线规划成功", vo));
    }
}
