package ntou.github.related.relationship;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.SwaggerNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "ACTING_ON")
public class ActingOn {

    @GraphId
    private Long relationshipId;

    @StartNode
    SwaggerNode swaggerNode;

    @EndNode
    APIEndpoint apiEndpoint;

    public ActingOn() {}

    public ActingOn(SwaggerNode swaggerNode, APIEndpoint apiEndpoint) {
        this.swaggerNode = swaggerNode;
        this.apiEndpoint = apiEndpoint;
    }
}
