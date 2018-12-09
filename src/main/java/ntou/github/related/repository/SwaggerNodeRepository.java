package ntou.github.related.repository;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import ntou.github.related.bean.SwaggerNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SwaggerNodeRepository extends GraphRepository<SwaggerNode> {

    @Query("MATCH (n:SwaggerNode {name:{name}})-->(r) RETURN r ")
    List<APIEndpoint> findByAPIEndpoint(@Param("name") String name);

    @Query("MATCH (n:SwaggerNode {name:{name}}) RETURN n ")
    List<SwaggerNode> findByName(@Param("name") String name);

    @Query("MATCH (n:SwaggerNode {name:{name}})-->(r)-->(m) RETURN m ")
    List<GithubNode> findGitHubNodeBySwaggerNode(@Param("name") String name);
}
