package ntou.github.related.bean;

import ntou.github.related.relationship.Find;
import ntou.github.related.relationship.Same;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;

@NodeEntity
public class GithubNode {


    @GraphId
    private Long id;

    private String repoFullName;

    private String repoName;

    private String repoUrl;

    private String javaDocumentName;

    private String javaDocumentUrl;

    private String javaDocumentPath;

    @Relationship(type="SAME")
    ArrayList<Same> sames = new ArrayList<Same>();

    public GithubNode() {
    }

    public GithubNode(String repoFullName, String repoName, String repoUrl, String javaDocumentName, String javaDocumentUrl, String javaDocumentPath) {
        this.repoFullName = repoFullName;
        this.repoName = repoName;
        this.repoUrl = repoUrl;
        this.javaDocumentName = javaDocumentName;
        this.javaDocumentUrl = javaDocumentUrl;
        this.javaDocumentPath = javaDocumentPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public void setRepoFullName(String repoFullName) {
        this.repoFullName = repoFullName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getJavaDocumentName() {
        return javaDocumentName;
    }

    public void setJavaDocumentName(String javaDocumentName) {
        this.javaDocumentName = javaDocumentName;
    }

    public String getJavaDocumentUrl() {
        return javaDocumentUrl;
    }

    public void setJavaDocumentUrl(String javaDocumentUrl) {
        this.javaDocumentUrl = javaDocumentUrl;
    }

    public String getJavaDocumentPath() {
        return javaDocumentPath;
    }

    public void setJavaDocumentPath(String javaDocumentPath) {
        this.javaDocumentPath = javaDocumentPath;
    }

    public void addSameRelationship(GithubNode githubNode, GithubNode githubNode1){
        Same same = new Same(githubNode, githubNode1);
        sames.add(same);
    }

}
