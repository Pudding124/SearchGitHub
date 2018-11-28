package ntou.github.related.web;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import ntou.github.related.PersonalInformation;
import ntou.github.related.bean.APIEndpoint;
import ntou.github.related.bean.GithubNode;
import ntou.github.related.bean.SwaggerNode;
import ntou.github.related.repository.APIEndpointRepository;
import ntou.github.related.repository.GithubNodeRepository;
import ntou.github.related.repository.SwaggerNodeRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class WebController {

    Logger log = LoggerFactory.getLogger(WebController.class);


    @Autowired
    APIEndpointRepository apiEndpointRepository;

    @Autowired
    SwaggerNodeRepository swaggerNodeRepository;

    SwaggerNode swaggerNode;
    APIEndpoint apiEndpoint;
    GithubNode githubNode;

    //static int rateLimit = 30;

    @RequestMapping(value = "/search/code", method = RequestMethod.GET)
    public String searchCode (@RequestParam("code") String code, APIEndpoint apiEndpoint) throws InterruptedException {
        //if(rateLimit == 0){
        //    log.info("Github API 次數用完，等待中...");
        //    Thread.sleep(1000*70);
        //    rateLimit = 30;
        //}
        while(true){
            log.info("code :{}",code);
            if(requestGithubAPI(code,apiEndpoint)){
                break;
            }else{
                log.info("請求失敗，重新請求");
            }
        }
        // github url

        return "Done";
    }

//    public void parseJson(JSONArray jsonArray, APIEndpoint apiEndpoint){
//
//        for(int i = 0;i < jsonArray.length();i++){
//            JSONObject item = jsonArray.getJSONObject(i);
//            JSONObject repo = item.getJSONObject("repository");
//            String name = repo.getString("name");
//            String htmlUrl = repo.getString("html_url");
//            //System.out.println(name + " " + htmlUrl);
//
//            githubNode = new GithubNode(repoFullName, repoName, repoHtmlUrl, javaDocumentName, javaDocumentUrl);
//            apiEndpoint.addFindRelationship(apiEndpoint, githubNode);
//            apiEndpointRepository.save(apiEndpoint);
//
//        }
//    }

    public void parseFragmentCode(JSONArray jsonArray, APIEndpoint apiEndpoint){
        for(int i = 0;i < jsonArray.length();i++){
            boolean saveCheck = false;
            JSONObject item = jsonArray.getJSONObject(i);
            JSONArray textMatches = item.getJSONArray("text_matches");
            String javaDocumentName = item.getString("name");
            String javaDocumentUrl = item.getString("url");
            String javaDocumentPath = item.getString("path");

            JSONObject repo = item.getJSONObject("repository");
            String repoName = repo.getString("name");
            String repoFullName = repo.getString("full_name");
            String repoHtmlUrl = repo.getString("html_url");


            for(int j = 0;j < textMatches.length();j++){
                JSONObject textMatch = textMatches.getJSONObject(j);
                String fragment = textMatch.getString("fragment");
                fragment = fragment.trim().toLowerCase();
                fragment = fragment.replaceAll("\n","");
                fragment = fragment.replaceAll("[\\pP\\p{Punct}]"," ");
                String str1[] = fragment.split(" ");
                String str2[] = apiEndpoint.getApiEndpoint().replaceAll("[\\pP\\p{Punct}]"," ").toLowerCase().split(" ");
                if(compareCode(str1,str2)){
                    saveCheck = true;
                    break;
                }
            }
            if(saveCheck) {
                log.info("success !");
                githubNode = new GithubNode(repoFullName, repoName, repoHtmlUrl, javaDocumentName, javaDocumentUrl, javaDocumentPath);
                apiEndpoint.addFindRelationship(apiEndpoint, githubNode);
                apiEndpointRepository.save(apiEndpoint);
            }
        }
    }

    public boolean compareCode(String[] str1, String[] str2){
        boolean httpControl = false;
        for(String key : str1) {
            if(key.equals("http") || key.equals("https")) {
                httpControl = true;
                break;
            }
        }
        if(!httpControl) {
            log.info("Miss on http or https");
            return false;
        }
        for(String key : str2){
            boolean flag = false;
            if(!key.equals("") && !key.equals("https")){
                for(String key1 : str1){
                    if(key.equals(key1)){
                        //System.out.println(key);
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    log.info("Miss on:{}", key);
                    return false;
                }
            }
        }
        return true;
    }

    public void parseSwagger(String swaggerDoc) throws IOException, InterruptedException {

        Swagger swagger = new SwaggerParser().parse(swaggerDoc); //解析Swagger文件

        String title = null;
        List schemes = null;
        String host = null;
        String basePath = null;
        String url = null;
        String endpoint = null;
        title = swagger.getInfo().getTitle();
        schemes = swagger.getSchemes();
        host = swagger.getHost();
        basePath = swagger.getBasePath();
        log.info("title:{}", title);
        log.info("schemes:{}", schemes);
        log.info("host:{}", host);
        log.info("basePath:{}", basePath);

        if(!title.isEmpty() && !schemes.isEmpty() && !host.isEmpty()){
            boolean flag = false;
            for(Object scheme : schemes){
                if(scheme.toString().toLowerCase().equals("https")){
                    flag = true;
                    saveSwaggerInfo(swagger,title,"https",host,basePath,url,endpoint);
                    break;
                }
            }
            if(!flag){
                saveSwaggerInfo(swagger,title,"http",host,basePath,url,endpoint);
            }
        }else{
            log.info("some information lost");
        }

    }

    public String parseSwaggerTitle(String swaggerDoc) throws IOException, InterruptedException {

        Swagger swagger = new SwaggerParser().parse(swaggerDoc); //解析Swagger文件

        String title = null;

        title = swagger.getInfo().getTitle();

        return title;
    }

    public void saveSwaggerInfo(Swagger swagger, String title, String scheme, String host, String basePath, String url, String endpoint){
        if(basePath == null){
            if(host.subSequence(host.length()-1,host.length()).equals("/")){
                url = scheme.toString().toLowerCase() + "://" + host.substring(0, host.length()-1);
            }else{
                url = scheme.toString().toLowerCase() + "://" + host;
            }
        }else{
            if(basePath.subSequence(basePath.length()-1,basePath.length()).equals("/")){
                url = scheme.toString().toLowerCase() + "://" + host + basePath.substring(0, basePath.length()-1);
            }else{
                url = scheme.toString().toLowerCase() + "://" + host + basePath;
            }
        }
        swaggerNode = new SwaggerNode(title, url);
        //swaggerNodeRepository.save(swaggerNode);
        for (String p : swagger.getPaths().keySet()) {
            if (swagger.getPaths().get(p).getDelete() != null) {
                endpoint = url+p;
                log.info("endpoint:{}",endpoint);

                apiEndpoint = new APIEndpoint("delete", endpoint);
                swaggerNode.addActingOnRelationship(swaggerNode, apiEndpoint);
                swaggerNodeRepository.save(swaggerNode);
                endpoint = endpoint.replaceAll("\\{", "");
                endpoint = endpoint.replaceAll("}", "");
                //searchCode(endpoint, apiEndpoint);
            }
            if (swagger.getPaths().get(p).getGet() != null) {
                endpoint = url+p;
                log.info("endpoint:{}",endpoint);

                apiEndpoint = new APIEndpoint("get", endpoint);
                swaggerNode.addActingOnRelationship(swaggerNode, apiEndpoint);
                swaggerNodeRepository.save(swaggerNode);
                endpoint = endpoint.replaceAll("\\{", "");
                endpoint = endpoint.replaceAll("}", "");
                //searchCode(endpoint, apiEndpoint);
            }
            if (swagger.getPaths().get(p).getPatch() != null) {
                endpoint = url+p;
                log.info("endpoint:{}",endpoint);

                apiEndpoint = new APIEndpoint("patch", endpoint);
                swaggerNode.addActingOnRelationship(swaggerNode, apiEndpoint);
                swaggerNodeRepository.save(swaggerNode);
                endpoint = endpoint.replaceAll("\\{", "");
                endpoint = endpoint.replaceAll("}", "");
                //searchCode(endpoint, apiEndpoint);
            }
            if (swagger.getPaths().get(p).getPost() != null) {
                endpoint = url+p;
                log.info("endpoint:{}",endpoint);

                apiEndpoint = new APIEndpoint("post", endpoint);
                swaggerNode.addActingOnRelationship(swaggerNode, apiEndpoint);
                swaggerNodeRepository.save(swaggerNode);
                endpoint = endpoint.replaceAll("\\{", "");
                endpoint = endpoint.replaceAll("}", "");
                //searchCode(endpoint, apiEndpoint);
            }
            if (swagger.getPaths().get(p).getPut() != null) {
                endpoint = url+p;
                log.info("endpoint:{}",endpoint);

                apiEndpoint = new APIEndpoint("put", endpoint);
                swaggerNode.addActingOnRelationship(swaggerNode, apiEndpoint);
                swaggerNodeRepository.save(swaggerNode);
                endpoint = endpoint.replaceAll("\\{", "");
                endpoint = endpoint.replaceAll("}", "");
                //searchCode(endpoint, apiEndpoint);
            }
        }
    }

    public boolean requestGithubAPI(String code, APIEndpoint apiEndpoint) throws InterruptedException {
        int page = 1;
        String url = new String("https://api.github.com/search/code?q="+code+"+language:java"+"&per_page=100&page="+page);

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
            headers.set("Accept","application/vnd.github.v3.text-match + json");
            headers.set("Authorization", authHeader);
            HttpEntity<String> requestEntity = new HttpEntity<String>("parameters", headers);
            log.info("等待 5 秒...");
            Thread.sleep(5000);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            //rateLimit--;
            //System.out.println(response.getBody());
            JSONArray items;
            JSONObject jsonObject;

            jsonObject = new JSONObject(response.getBody());
            //int total = jsonObject.getInt("total_count");

            items = jsonObject.getJSONArray("items");
            //parseJson(items, apiEndpoint); // SearchGitHub class 使用
            parseFragmentCode(items, apiEndpoint); // SearchGitHubRealUse class 使用
        }catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
            log.info("停止 20 秒");
            Thread.sleep(20000);
            return false;
        }
        return true;
    }
}
