package ntou.github.related;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.SwaggerNode;
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
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GuruLocalFile {
    Logger log = LoggerFactory.getLogger(GuruLocalFile.class);

    @Autowired
    WebController webController;

    @Test
    public void readGuruFiles(){
        File sDocFolder = new File("./src/main/resources/GuruSwaggerDoc");
        for (String serviceFile : sDocFolder.list()) {
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/GuruSwaggerDoc/" + serviceFile);
                if(document != null){
                    webController.parseSwagger(document);
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/GuruSwaggerDoc/" + serviceFile), Paths.get("./src/main/resources/finish/" + serviceFile));
                log.info("finish move file {} to finish folder.", serviceFile);
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
                try {
                    Files.move(Paths.get("./src/main/resources/GuruSwaggerDoc/" + serviceFile), Paths.get("./src/main/resources/parseError/" + serviceFile));
                } catch (IOException e1) {
                    log.info("error on move file to error folder", e);
                }
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

    //@Test
    public void parseOneFile(){
        String serviceFile = "adafruit.com_2.0.0.json";

        try {
            // do something
            String document = readLocalSwagger("./src/main/resources/parseError/" + serviceFile);
            if(document != null){
                webController.parseSwagger(document);
            }else{
                log.error("error read swagger local file: {}", serviceFile);
            }
            // Files.move(Paths.get("./src/main/resources/swagger/guru/" + serviceFile), Paths.get("./src/main/resources/swagger/finish/" + serviceFile));
        } catch (Exception e) {
            log.error("error parsing on {}", serviceFile, e);
        }
    }

}
