package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_kb.Documents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Documents, Integer> {
}
