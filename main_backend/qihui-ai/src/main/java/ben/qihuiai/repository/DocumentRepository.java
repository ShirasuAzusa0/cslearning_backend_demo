package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_kb.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Documents, Integer> {
    @Query(value = """
            SELECT d
            FROM Documents d
            WHERE d.documentId = :documentId
            ORDER BY d.documentLoadedAt ASC
            """)
    Documents findDocDetails(@Param("documentId") long documentId);

    @Query(value = """
            SELECT d, dk
            FROM Documents d
            JOIN  doc_kb dk ON dk.document.documentId = d.documentId
            WHERE dk.kb.kbId = :kbId
            ORDER BY d.documentLoadedAt ASC
            """)
    List<Documents> findDocListByKbId(@Param("kbId") int kbId);

    @Modifying
    void deleteByDocumentId(@Param("documentId") long documentId);

    Documents findByDocumentId(@Param("documentId") long documentId);
}
