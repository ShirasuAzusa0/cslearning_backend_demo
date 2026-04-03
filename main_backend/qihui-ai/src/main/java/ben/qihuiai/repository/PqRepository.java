package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.paragraph_questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PqRepository extends JpaRepository<paragraph_questions, Integer> {
    @Query(value = "SELECT pq FROM paragraph_questions pq WHERE pq.paragraph.paragraphId = :paragraphId")
    List<paragraph_questions> findByParagraphId(@Param("paragraphId") int paragraphId);
}
