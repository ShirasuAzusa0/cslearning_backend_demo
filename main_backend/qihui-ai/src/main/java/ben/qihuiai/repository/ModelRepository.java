package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_chat.Models;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelRepository extends JpaRepository<Models, Integer> {
    Models findByModelId(@Param("modelId") int modelId);

    Models findByModelName(@Param("modelName") String modelName);

    @Query(value = """
            SELECT m
            FROM Models m
            WHERE m.modelType = "local"
            """)
    List<Models> findLocalModels();

    @Query(value = """
            SELECT m
            FROM Models m
            WHERE m.modelType = "api"
            """)
    List<Models> findApiModels();
}
