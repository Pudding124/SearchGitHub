package ntou.github.related;

import ntou.github.related.GuruLocalFile;
import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import ntou.github.related.bean.SwaggerNode;
import ntou.github.related.repository.APIEndpointRepository;
import ntou.github.related.repository.GithubNodeRepository;
import ntou.github.related.repository.SwaggerNodeRepository;
import ntou.github.related.web.WebController;
import ntou.github.related.web.WriteTxt;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BuildGitHubNodeRelationship {

    Logger log = LoggerFactory.getLogger(GuruLocalFile.class);

    @Autowired
    WebController webController;

    @Autowired
    SwaggerNodeRepository swaggerNodeRepository;

    @Autowired
    APIEndpointRepository apiEndpointRepository;

    @Autowired
    GithubNodeRepository githubNodeRepository;

    @Autowired
    WriteTxt writeTxt;

    @Test
    public void buildGitHubNodeRelationship(){
        int fileNumber = 10;
        ArrayList<String> stopList = new ArrayList<>();
        File sDocFolder = new File("./src/main/resources/finishsearch");
        for (String serviceFile : sDocFolder.list()) {
            if(fileNumber == 0) break;
            fileNumber--;
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/finishsearch/" + serviceFile);
                if(document != null){
                    String title = webController.parseSwaggerTitle(document);
                    addGitHubNode(title, stopList);
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/allDone/" + serviceFile));
                log.info("finish move file {} to finish search folder.", serviceFile);
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
                log.info(e.toString());
                try {
                    Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/notDone/" + serviceFile));
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

    public void addGitHubNode(String title, ArrayList stopList) throws ExecutionException, InterruptedException {
        for(APIEndpoint apiEndpoint : swaggerNodeRepository.findByAPIEndpoint(title)){ // 搜尋每個 Swagger Doc 的 Endpoint
            String endpoint = apiEndpoint.getApiEndpoint(); // 起始 API Endpoint
            String operation = apiEndpoint.getOperation();
            String repoName = null;
            String url = null;
            ArrayList<String> endpointList = new ArrayList<>();
            for(GithubNode githubNode : apiEndpointRepository.findByGitHubNode(endpoint)){ // 搜尋每個 Endpoint 的 Github Node
                //githubNode = new GithubNode();
                repoName = githubNode.getRepoName();
                url = githubNode.getRepoUrl();
                if(stopList.contains(repoName)){
                    continue;
                }

                for(APIEndpoint apiEndpoint1 : githubNodeRepository.findByConnectNode(repoName)){
                    String word = apiEndpoint1.getApiEndpoint();
                    log.info("Endpoint :{}",word);
                    if(endpointList.contains(word)){
                        log.info("相同");
                        continue;
                    }else{
                        endpointList.add(word);
                        log.info("不相同");
                    }
                }
                stopList.add(repoName);
//                for(GithubNode githubNode1 : githubNodeRepository.findBySameGitHubNode(repoName)){ // 搜尋相同的 Github Node
//                    //githubNode1 = new GithubNode();
//                    String repoName1 = githubNode1.getRepoName();
//
//                    for(APIEndpoint apiEndpoint1 : githubNodeRepository.findByConnectNode(repoName1)){ // 檢查相同的 Github Node 的 Endpoint 是否相同
//                        if(apiEndpoint1.getApiEndpoint().equals(endpoint)){
//                            if(apiEndpoint1.getOperation().equals(operation)){
//                                log.info(Thread.currentThread().getName()+"相同，不建立關係");
//                            }else{
//                                log.info(Thread.currentThread().getName()+"操作不同，建立關係");
//                                githubNode.addSameRelationship(githubNode, githubNode1);
//                                githubNodeRepository.save(githubNode);
//                            }
//                        }else{
//                            log.info(Thread.currentThread().getName()+"全部不同，建立關係");
//                            githubNode.addSameRelationship(githubNode, githubNode1);
//                            githubNodeRepository.save(githubNode);
//                        }
//                    }
//                }
            }
            if(repoName != null){
                writeTxt.inputTxt(repoName+" "+url+"\r\n");
            }
            if(endpointList.size() >= 2){
                for(String str : endpointList){
                    writeTxt.inputTxt(str+" "+"\r\n");
                }
            }
            writeTxt.inputTxt("\r\n");
        }
    }

//    public void addRelationship(GithubNode githubNode, GithubNode githubNode1) throws ExecutionException, InterruptedException {
////        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
////        Future future = fixedThreadPool.submit(new Callable() {
////            public Object call() throws Exception  {
////                System.out.println(Thread.currentThread().getName() + "执行");
////                githubNode.addSameRelationship(githubNode, githubNode1);
////                githubNodeRepository.save(githubNode);
////                return "OK";
////            }
////        });
////        log.info("future.get() = :{}",future.get());
////        fixedThreadPool.shutdown();
////    }
}
