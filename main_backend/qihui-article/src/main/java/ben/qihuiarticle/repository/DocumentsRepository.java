package ben.qihuiarticle.repository;

import ben.qihuiarticle.entity.entity_document.Documents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentsRepository extends JpaRepository<Documents, Integer> {

    Documents findByDocumentId(long documentId);
}
