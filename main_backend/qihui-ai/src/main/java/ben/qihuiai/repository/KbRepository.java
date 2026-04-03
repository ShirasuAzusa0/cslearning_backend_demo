package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.KnowledgeBases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KbRepository extends JpaRepository<KnowledgeBases, Integer> {
    @Query(value = """
            SELECT *
            FROM knowledge_bases
            WHERE userId = :userId
            ORDER BY createdAt ASC
            """, nativeQuery = true)
    List<KnowledgeBases> findAllByUserId(@Param("userId") long userId);

    @Query("""
        SELECT kb, u, m
        FROM KnowledgeBases kb
        JOIN kb.user u
        JOIN kb.embeddingModel m
        WHERE u.userId = :userId
        ORDER BY kb.createdAt ASC
        """)
    KnowledgeBases findKbByUserId(@Param("userId") long userId);

    KnowledgeBases findByKbId(@Param("kbId") int kbId);
}
