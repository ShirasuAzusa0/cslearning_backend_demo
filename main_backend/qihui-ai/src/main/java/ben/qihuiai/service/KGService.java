package ben.qihuiai.service;

import ben.qihuiai.entity.entity_graph.crawler_datas;
import ben.qihuiai.entity.vo.*;
import ben.qihuiai.repository.CrawlerRepository;
import ben.qihuiai.repository.GraphRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ben.qihuiai.util.jsonUtil.VideoToJSON;

@Service
public class KGService {

    private final CrawlerRepository crawlerRepository;
    private final GraphRepository graphRepository;

    public KGService(CrawlerRepository crawlerRepository, GraphRepository graphRepository) {
        this.crawlerRepository = crawlerRepository;
        this.graphRepository = graphRepository;
    }

    // 获取多模态学习资源
    public MultiResourceVO getMultiLearningResources(String nodeName) {
        // 获取学习文档
        List<ArticleProfileVO> articles = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = "classpath*:节点文章/**/" + nodeName + "/**/*.md";
            Resource[] mdFiles = resolver.getResources(pattern);
            for (Resource mdFile : mdFiles) {
                String fileName = mdFile.getFilename();
                String articleName = null;
                if (fileName != null) {
                    articleName = fileName.substring(0, fileName.lastIndexOf("."));
                }
                String fullUrl = mdFile.getURL().toString();
                int index = fullUrl.lastIndexOf("节点文章/");
                String articlePath = fullUrl.substring(index).replace(".md", "");
                articles.add(new ArticleProfileVO(articleName, articlePath));
            }
        } catch (IOException e) {
            throw new RuntimeException("获取学习文档失败", e);
        }

        // 获取视频资源
        List<VideoVO> videos = new ArrayList<>();
        List<crawler_datas> cds = crawlerRepository.findByDataId(nodeName);
        for (crawler_datas cd : cds) {
            videos.add(VideoToJSON(cd.getCrawlerData()));
        }

        return new MultiResourceVO(
                articles,
                videos
        );
    }

    // 获取完整知识图谱的节点与关系
    public List<NodeRelVO> getAllNodes() {
        return graphRepository.getWholeGraphRelationships();
    }

    // 获取节点下分的关系
    public List<NodeRelVO> getNextLevelNodes(String nodeName) {
        return graphRepository.getAllDescendantRelationships(nodeName);
    }

    // 获取节点同级的关系
    public List<NodeRelVO> getSameLevelNodes(String nodeName) {
        NodeVO node = graphRepository.getNodeProfile(nodeName);
        if (node == null) {
            return null;
        }
        return graphRepository.getSameLevelRelationships(Integer.toString(node.getLevel()));
    }
}
