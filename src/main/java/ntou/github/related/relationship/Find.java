package ntou.github.related.relationship;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import ntou.github.related.bean.SwaggerNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "FIND")
public class Find {

    @GraphId
    private Long relationshipId;

    @StartNode
    APIEndpoint apiEndpoint;

    @EndNode
    GithubNode githubNode;

    public Find() {}

    public Find(APIEndpoint apiEndpoint, GithubNode githubNode) {
        this.apiEndpoint = apiEndpoint;
        this.githubNode = githubNode;
    }

}
