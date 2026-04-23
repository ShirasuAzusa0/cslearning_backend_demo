package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_chat.Sessions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends CrudRepository<Sessions, Integer> {
    @Query(value = """
            SELECT s
            FROM Sessions s
            WHERE s.user.userId = :userId
            """)
    List<Sessions> findAllByUserId(@Param("userId") long userId);

    Sessions getSessionsBySessionId(@Param("sessionId") int sessionId);
}
