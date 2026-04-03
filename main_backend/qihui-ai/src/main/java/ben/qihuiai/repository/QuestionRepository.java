package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Questions, Long> {
    @Query(value = """
            SELECT pq.question
                FROM paragraph_questions pq
                WHERE pq.paragraph.paragraphId = :paragraphId
            """)
    List<Questions> findByParagraphs(@Param("paragraphId") long paragraphId);
}
