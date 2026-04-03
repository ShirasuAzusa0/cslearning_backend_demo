package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.KnowledgeBases;
import ben.qihuiai.entity.entity_kb.Paragraphs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParagraphRepository extends JpaRepository<Paragraphs, Integer> {

    @Modifying
    void deleteByKb(KnowledgeBases kb);

    @Query(value = """
            SELECT p
            FROM Paragraphs p
            WHERE p.kb.kbId = :kbId
            ORDER BY p.splitAt DESC
            """)
    List<Paragraphs> findByKbId(@Param("kbId") int kbId);

    @Modifying
    @Query(value = """
            DELETE
            FROM Paragraphs p
            WHERE p.document.documentId = :documentId AND p.kb.kbId = :kbId
            """)
    void deleteByDocumentAndKb(@Param("documentId") long documentId, @Param("kbId") int kbId);
}
