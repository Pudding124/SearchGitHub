package ntou.github.related.repository;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface APIEndpointRepository extends GraphRepository<APIEndpoint> {
    @Query("MATCH (n:APIEndpoint {apiEndpoint:{apiEndpoint}})-->(r) RETURN r ")
    List<GithubNode> findByGitHubNode(@Param("apiEndpoint") String apiEndpoint);
}
