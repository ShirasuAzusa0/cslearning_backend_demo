package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_log.Neo4jLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Neo4jLogRepository extends JpaRepository<Neo4jLog, Long> {
}
