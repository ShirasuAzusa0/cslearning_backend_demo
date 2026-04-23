package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_chat.Models;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Models, Integer> {
}
