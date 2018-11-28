package ntou.github.related;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.SwaggerNode;
import ntou.github.related.repository.APIEndpointRepository;
import ntou.github.related.repository.SwaggerNodeRepository;
import ntou.github.related.web.WebController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchGitHub {

    Logger log = LoggerFactory.getLogger(SearchGitHub.class);


    @Autowired
    APIEndpointRepository apiEndpointRepository;

    @Autowired
    SwaggerNodeRepository swaggerNodeRepository;

    @Autowired
    WebController webController = new WebController();

    @Test
    public void readFinishFile(){
        int fileNumber = 200;
        File sDocFolder = new File("./src/main/resources/finish");
        for (String serviceFile : sDocFolder.list()) {
            if(fileNumber == 0) break;
            fileNumber--;
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/finish/" + serviceFile);
                if(document != null){
                    String title = webController.parseSwaggerTitle(document);
                    addGitHubNode(title);
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/finish/" + serviceFile), Paths.get("./src/main/resources/finishsearch/" + serviceFile));
                log.info("finish move file {} to finish search folder.", serviceFile);
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
                try {
                    Files.move(Paths.get("./src/main/resources/finish/" + serviceFile), Paths.get("./src/main/resources/finishfailsearch/" + serviceFile));
                } catch (IOException e1) {
                    log.info("error on move file to error folder", e);
                }
            }
        }
    }

    public void addGitHubNode(String title){

        for(APIEndpoint apiEndpoint : swaggerNodeRepository.findByAPIEndpoint(title)){
            try {
                String endpoint = apiEndpoint.getApiEndpoint();
                boolean flag = true;
                for(int i = 0;i < endpoint.length();i++){
                    if(endpoint.substring(i, i+1).equals("{") || endpoint.substring(i, i+1).equals("}")){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    if(apiEndpoint.getApiEndpoint() != null){
                        log.info("API Enpoint : {}", endpoint);
                        webController.searchCode(endpoint, apiEndpoint);
                    }else{
                        log.info("有 API Endpoint 是空值");
                    }
                }
            } catch (InterruptedException e) {
                log.info("Search Code Error");
            }
        }
    }

    // For testing
    public String readLocalSwagger(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            System.err.println("read swagger error");
            return null;
        }

    }
}
