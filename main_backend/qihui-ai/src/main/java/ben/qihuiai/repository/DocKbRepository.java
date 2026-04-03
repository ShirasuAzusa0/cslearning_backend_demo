package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.KnowledgeBases;
import ben.qihuiai.entity.entity_kb.doc_kb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocKbRepository extends JpaRepository<doc_kb, Integer> {

    @Modifying
    void deleteByKb(KnowledgeBases kb);

    @Query(value = "SELECT dk FROM doc_kb dk WHERE dk.document.documentId = :documentId")
    doc_kb findByDocumentId(@Param("documentId") long documentId);

    @Modifying
    @Query(value = """
            DELETE
            FROM doc_kb dk
            WHERE dk.document.documentId = :documentId AND dk.kb.kbId = :kbId
            """)
    void deleteByDocumentAndKb(@Param("documentId") long documentId, @Param("kbId") int kbId);
}
