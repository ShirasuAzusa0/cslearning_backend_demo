package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_chat.Sessions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends CrudRepository<Sessions, Integer> {
    @Query(value = """
            SELECT *
            FROM sessions
            WHERE userId = :userId
            """, nativeQuery = true)
    List<Sessions> findAllByUserId(@Param("userId") long userId);

    Sessions getSessionsBySessionId(@Param("sessionId") int sessionId);
}
