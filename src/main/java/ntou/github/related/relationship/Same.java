package ntou.github.related.relationship;

import ntou.github.related.bean.GithubNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "SAME")
public class Same {

    @GraphId
    private Long relationshipId;

    @StartNode
    GithubNode githubNode;

    @EndNode
    GithubNode githubNode1;

    public Same() {}

    public Same(GithubNode githubNode, GithubNode githubNode1) {
        this.githubNode = githubNode;
        this.githubNode1 = githubNode1;
    }

}
