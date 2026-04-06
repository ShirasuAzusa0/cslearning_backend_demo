package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.ChunkingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChunkingRulesRepository extends JpaRepository<ChunkingRules, Integer> {
    @Query(value = "SELECT cr FROM ChunkingRules cr")
    List<ChunkingRules> findAllRules();
}
