package ben.qihuiarticle.service;

import ben.qihuiarticle.entity.entity_document.Documents;
import ben.qihuiarticle.entity.entity_user.LPJsonFormat;
import ben.qihuiarticle.entity.entity_user.Users;
import ben.qihuiarticle.entity.vo.ArticleProfileVO;
import ben.qihuiarticle.entity.vo.ArticleVO;
import ben.qihuiarticle.entity.vo.NodeRelVO;
import ben.qihuiarticle.repository.DocumentsRepository;
import ben.qihuiarticle.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ben.qihuiarticle.util.jsonUtil.LPtoJSON;

@Service
public class ArticleService {
    private final UsersRepository usersRepository;
    private final DocumentsRepository documentsRepository;

    public ArticleService(UsersRepository usersRepository, DocumentsRepository documentsRepository) {
        this.usersRepository = usersRepository;
        this.documentsRepository = documentsRepository;
    }

    // 获取文档对应的小型知识图谱
    private List<NodeRelVO> getArticleGraph(String articleName) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // md → graph.json，通过articleName获取对应的知识图谱
            String pattern = "classpath*:节点文章图谱数据/**/" + articleName + ".json";

            Resource[] resources = resolver.getResources(pattern);

            if (resources.length == 0) {
                return Collections.emptyList();
            }

            if (resources.length > 1) {
                throw new RuntimeException("图谱文件不唯一");
            }

            Resource resource = resources[0];

            String json = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();

            return Arrays.asList(objectMapper.readValue(json, NodeRelVO[].class));

        } catch (IOException e) {
            throw new RuntimeException("获取知识图谱失败", e);
        }
    }

    // 获取文章具体内容
    public ArticleVO getArticleByArticleName(String articleName, String articlePath) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = "classpath*:" + articlePath + ".md";
            Resource[] resources = resolver.getResources(pattern);
            if (resources.length == 0) {
                throw new RuntimeException("文章不存在");
            }
            Resource resource = resources[0];

            // 解析读取 Markdown
            String markdown = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);


            return new ArticleVO(articleName.substring(0, articleName.lastIndexOf(".")),
                    markdown,
                    getArticleGraph(articleName.substring(0, articleName.lastIndexOf(".")))
            );
        } catch (IOException e) {
            throw new RuntimeException("读取文章失败", e);
        }
    }

    // 获取指定文章概要信息
    public ArticleProfileVO getArticleProfileByDocumentId(int documentId) {
        try {
            Documents documents = documentsRepository.findByDocumentId(documentId);
            if (documents == null) {
                throw new RuntimeException("文档不存在，documentId: " + documentId);
            }

            String filename = documents.getDocumentName();
            String articleName = filename.substring(0, filename.lastIndexOf("."));

            // 扫描整个节点文章目录，查找对应的文件
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:节点文章/**/*.md");

            for (Resource resource : resources) {
                String resourceFilename = resource.getFilename();
                if (resourceFilename != null && resourceFilename.equals(filename)) {
                    // 找到对应的文件，获取相对路径
                    String relativePath = getString(resource, articleName);
                    return new ArticleProfileVO(articleName, relativePath);
                }
            }

            throw new RuntimeException("未找到文章文件: " + filename);

        } catch (IOException e) {
            throw new RuntimeException("获取文章失败，documentId: " + documentId, e);
        }
    }

    // 获取文章概要列表
    public List<ArticleProfileVO> getArticleProfileList() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 只扫描 节点文章 目录
            Resource[] resources = resolver.getResources("classpath*:节点文章/**/*.md");

            return Arrays.stream(resources)
                    .map(resource -> {
                        try {
                            String filename = resource.getFilename();
                            if (filename == null) {
                                return null;
                            }
                            // 文章名（去掉 .md）
                            String articleName = filename.substring(0, filename.lastIndexOf("."));
                            // 获取业务路径
                            String relativePath = getString(resource, articleName);
                            return new ArticleProfileVO(articleName, relativePath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("获取文章列表失败", e);
        }
    }

    private static String getString(Resource resource, String articleName) throws IOException {
        String fullPath = resource.getURL().toString();

        // 解码 URL
        fullPath = URLDecoder.decode(fullPath, StandardCharsets.UTF_8);

        // 截取 "节点文章/" 后面的路径
        int index = fullPath.indexOf("节点文章/");
        String relativePath;

        if (index != -1) {
            relativePath = fullPath.substring(index);
        } else {
            relativePath = articleName;
        }

        // 去掉 .md 后缀
        relativePath = relativePath.replace(".md", "");
        return relativePath;
    }

    // 获取定制化推荐学习文档列表
    public List<ArticleProfileVO> getRecommendListByUserId(long userId) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Users user = usersRepository.findByUserId(userId);
            if (user.getLearningPath() == null) {
                Resource resource = resolver.getResource("classpath:basicArticle.json");
                ObjectMapper mapper = new ObjectMapper();
                ArticleProfileVO[] array =
                        mapper.readValue(resource.getInputStream(), ArticleProfileVO[].class);
                List<ArticleProfileVO> list = new ArrayList<>(Arrays.asList(array));
                // 打乱顺序
                Collections.shuffle(list);
                // 取最多三篇
                return list.stream()
                        .limit(3)
                        .toList();
            }

            List<LPJsonFormat> learningPath = LPtoJSON(user.getLearningPath());

            // 提取用户定制化学习路线中的所有节点名并去重
            Set<String> nodeSet = new HashSet<>();
            for (LPJsonFormat lp : learningPath) {
                nodeSet.add(lp.getStartNode().getName());
                nodeSet.add(lp.getEndNode().getName());
            }

            // 获取“节点文章”下所有目录
            Resource[] allResources = resolver.getResources("classpath*:节点文章/**");
            List<Resource> matchedMdFiles = new ArrayList<>();
            for (Resource resource : allResources) {
                String url = resource.getURL().toString();

                for (String nodeName : nodeSet) {
                    // 若路径包含节点名，则表示匹配到目录
                    if (url.contains(nodeName)) {
                        // 此时扫描该目录下的所有 markdown 文件
                        String pattern = "classpath*:" +
                                url.substring(url.indexOf("节点文章/")) +
                                "/*.md";
                        Resource[] mdFiles = resolver.getResources(pattern);
                        matchedMdFiles.addAll(Arrays.asList(mdFiles));
                    }
                }
            }
            if (matchedMdFiles.isEmpty()) {
                return Collections.emptyList();
            }

            // 打乱顺序，随机抽取三篇作为返回
            Collections.shuffle(matchedMdFiles);

            return matchedMdFiles.stream()
                    .limit(3)
                    .map(md -> {
                        try {
                            String filename = md.getFilename();
                            String articleName = null;
                            if (filename != null) {
                                articleName = filename.substring(0, filename.lastIndexOf("."));
                            }
                            String fullUrl = md.getURL().toURI().toString();
                            int index = fullUrl.indexOf("节点文章/");
                            String relativePath = fullUrl.substring(index)
                                    .replace(".md", "");

                            return new ArticleProfileVO(articleName, relativePath);
                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("推荐文章获取失败", e);
        }
    }
}
