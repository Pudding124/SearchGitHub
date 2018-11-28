package ntou.github.related.bean;


import ntou.github.related.relationship.ActingOn;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;

@NodeEntity
public class SwaggerNode {

    @GraphId
    private Long id;

    private String name;

    private String baseUrl;

    @Relationship(type="ACTING_ON")
    ArrayList<ActingOn> actingOns = new ArrayList<ActingOn>();

    public SwaggerNode(){}

    public SwaggerNode(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void addActingOnRelationship(SwaggerNode swaggerNode, APIEndpoint apiEndpoint){
        ActingOn actingOn = new ActingOn(swaggerNode, apiEndpoint);
        actingOns.add(actingOn);
    }
}
