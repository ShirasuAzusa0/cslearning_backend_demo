package ben.qihuiai.entity.entity_graph;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "crawler_datas")
public class crawler_datas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawlerId")
    private int crawlerId;

    @Column(name = "dataId", nullable = false)
    private String dataId;

    @Column(name = "crawlerData", nullable = false, columnDefinition = "JSON")
    private String crawlerData;
}
