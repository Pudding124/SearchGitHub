package ntou.github.related.repository;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GithubNodeRepository extends GraphRepository<GithubNode> {

    @Query("MATCH (n:GithubNode {repoName:{repoName}})<--(r) RETURN r ")
    List<APIEndpoint> findByConnectNode(@Param("repoName") String repoName);
}
