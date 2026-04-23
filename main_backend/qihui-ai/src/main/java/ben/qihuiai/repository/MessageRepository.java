package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_chat.Messages;
import ben.qihuiai.entity.entity_chat.Sessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Integer> {
    @Query(value = """
            SELECT m
            FROM Messages m
            WHERE m.session.sessionId = :sessionId
            Order by m.createdAt ASC
            """)
    List<Messages> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") int sessionId);

    void deleteBySession(Sessions session);

    @Modifying
    void deleteByMessageId(@Param("messageId") long messageId);
}
