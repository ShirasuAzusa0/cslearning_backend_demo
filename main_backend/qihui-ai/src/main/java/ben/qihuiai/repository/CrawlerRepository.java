package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_graph.crawler_datas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrawlerRepository extends JpaRepository<crawler_datas, Integer> {
    @Query("SELECT cd FROM crawler_datas cd WHERE cd.dataId = :dataId")
    List<crawler_datas> findByDataId(@Param("dataId") String dataId);
}
