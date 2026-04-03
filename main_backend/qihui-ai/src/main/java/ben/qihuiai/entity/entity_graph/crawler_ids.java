package ben.qihuiai.entity.entity_graph;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "crawler_ids")
public class crawler_ids {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "dataId", referencedColumnName = "dataId")
    private crawler_datas dataTag;
}
