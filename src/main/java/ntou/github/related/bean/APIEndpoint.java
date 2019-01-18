package ntou.github.related.bean;

import ntou.github.related.relationship.Find;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;


@NodeEntity
public class APIEndpoint {

    @GraphId
    private Long id;

    private String operation;

    private String apiEndpoint;

    @Relationship(type="FIND")
    ArrayList<Find> finds = new ArrayList<Find>();

    public APIEndpoint() {}

    public APIEndpoint(String operation, String apiEndpoint) {
        this.operation = operation;
        this.apiEndpoint = apiEndpoint;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public void addFindRelationship(APIEndpoint apiEndpoint, GithubNode githubNode){
        Find find = new Find(apiEndpoint, githubNode);
        finds.add(find);
    }
}
