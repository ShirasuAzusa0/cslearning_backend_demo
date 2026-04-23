package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_kb.Paragraphs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParagraphRepository extends JpaRepository<Paragraphs, Integer> {
}
