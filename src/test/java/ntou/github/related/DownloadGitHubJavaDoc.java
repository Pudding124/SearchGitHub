package ntou.github.related;

import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import ntou.github.related.bean.SwaggerNode;
import ntou.github.related.repository.APIEndpointRepository;
import ntou.github.related.repository.SwaggerNodeRepository;
import ntou.github.related.web.WebController;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DownloadGitHubJavaDoc {
    Logger log = LoggerFactory.getLogger(DownloadGitHubJavaDoc.class);

    @Autowired
    APIEndpointRepository apiEndpointRepository;

    @Autowired
    SwaggerNodeRepository swaggerNodeRepository;

    @Autowired
    WebController webController;

    @Test
    public void readGuruFiles(){
        int fileNumber = 1;
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
                    downloadGitHubDoc(swaggerNodeRepository.findByName(title));
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/downloadFinish/" + serviceFile));
                log.info("finish move file {} to finish search folder.", serviceFile);
            } catch (Exception e) {
                log.error("error download on {}", serviceFile);
                try {
                    Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/downloadFail/" + serviceFile));
                } catch (IOException e1) {
                    log.info("error on move file to error folder", e);
                }
            }
        }
    }

    public void downloadGitHubDoc(List<SwaggerNode> swaggerList) {


        for(SwaggerNode swaggerNode : swaggerList) {
            String swaggerName = swaggerNode.getName();

            for(APIEndpoint apiEndpoint : swaggerNodeRepository.findByAPIEndpoint(swaggerName)) {
                String api = apiEndpoint.getApiEndpoint();
                String folderName = swaggerName.replaceAll("/", "");
                File dir_file = new File("/home/mingjen/Desktop/DownloadGitHubJavaDoc/"+folderName);

                if(!dir_file.exists()) {
                    dir_file.mkdir();
                }

                for(GithubNode githubNode : apiEndpointRepository.findByGitHubNode(api)) {
                    Long id = githubNode.getId();
                    String javaName = githubNode.getJavaDocumentName();
                    String repoFullName = githubNode.getRepoFullName();
                    String javaDocumentPath = githubNode.getJavaDocumentPath();

                    // avoid my repo affect result
                    if(!repoFullName.equals("Pudding124/DataClassification")) {
                        String url = new String("https://api.github.com/repos/"+repoFullName+"/contents/"+javaDocumentPath);
                        while (true){
                            if(sendRequestAndDownloadJavaDoc(id, url, javaName, folderName)) {
                                break;
                            }else{
                                log.info("請求失敗，重新請求");
                            }
                        }
                    }
                }
            }
        }
    }

//    public void getUrlAndDownload(Long id, String downloadUrl, String javaName) throws IOException, JSONException {
//
//
//        BufferedReader in;
//
//        URLConnection connection = new URL(downloadUrl).openConnection();
//
//        connection.setRequestProperty("Content-Type", "application/json");
//        //设置用户代理
//        connection.setRequestProperty("User-agent", "	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
//
//        in = new BufferedReader(
//                new InputStreamReader(connection.getInputStream()));
//
//        String result;
//        StringBuilder sb = new StringBuilder();
//        while ((result = in.readLine()) != null){
//            sb.append(result);
//        }
//        // System.out.println(result2);
//        JSONObject jsonObject = new JSONObject(sb.toString());
//        String download = jsonObject.getString("download_url");
//
//        URLConnection connection2 = new URL(download).openConnection();
//        connection2.setRequestProperty("Content-Type", "application/json");
//        //设置用户代理
//        connection2.setRequestProperty("User-agent", "	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
//
//        in = new BufferedReader(
//                new InputStreamReader(connection2.getInputStream()));
//        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/mingjen/Desktop/DownloadGitHubJavaDoc/"+id+"-"+javaName)));
//
//        String inputLine;
//        while ((inputLine = in.readLine()) != null){
//            //System.out.println(inputLine);
//            bw.write(inputLine+"\r\n");
//        }
//        in.close();
//        bw.close();
//    }

    public boolean sendRequestAndDownloadJavaDoc(Long id, String downloadUrl, String javaName, String folderName) {
        // Auth
        String auth = PersonalInformation.GITHUB_ACCOUNT + ":" + PersonalInformation.GITHUB_PASSWORD;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );



        try {
            // Request
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            headers.add("Retry-After","3");
            headers.set("Accept","application/vnd.github.VERSION.raw");
            headers.set("Authorization", authHeader);
            HttpEntity<String> requestEntity = new HttpEntity<String>("parameters", headers);
            log.info("DownloadUrl :{}", downloadUrl);
            log.info("等待 2 秒...");
            Thread.sleep(2000);
            ResponseEntity<String> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, requestEntity, String.class);

//            JSONObject jsonObject = new JSONObject(response.getBody());
//            String download = jsonObject.getString("download_url");
//            response = restTemplate.exchange(download, HttpMethod.GET, requestEntity, String.class);

            File file = new File("/home/mingjen/Desktop/DownloadGitHubJavaDoc/"+folderName+"/"+id+"-"+javaName);

            // avoid file overwrite, some swagger title is same
            if(!file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(response.getBody());
                fileWriter.flush();
                fileWriter.close();
            }

        }catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode().toString());
            System.out.println(e.getResponseBodyAsString());
            if (e.getStatusCode().toString().equals("404")) {
                log.info("file maybe delete by user");
                return true;
            }
            log.info("停止 20 秒");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                log.info("InterruptedException :{}", e1.toString());
            }
            return false;
        } catch (InterruptedException e) {
            log.info("InterruptedException :{}", e.toString());
        } catch (IOException e) {
            log.info("IOException :{}", e.toString());
        }
        return true;
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
// 2018-11-25 13:17:30.158  INFO 4654 --- [           main] n.github.related.DownloadGitHubJavaDoc   : DownloadUrl :https://api.github.com/repos/RinatB2017/Android_github/contents/_GPS/gpslogger/gpslogger/src/main/java/com/mendhak/gpslogger/senders/googledrive/GoogleDriveJob.java