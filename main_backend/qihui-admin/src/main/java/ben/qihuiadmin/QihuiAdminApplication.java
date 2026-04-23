package ben.qihuiadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@EnableFeignClients
@SpringBootApplication(exclude = {
        Neo4jReactiveDataAutoConfiguration.class,
        Neo4jReactiveRepositoriesAutoConfiguration.class
})
@EnableNeo4jRepositories(
        basePackages = "ben.qihuiadmin.repository",
        considerNestedRepositories = true
)
public class QihuiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(QihuiAdminApplication.class, args);
    }

}
