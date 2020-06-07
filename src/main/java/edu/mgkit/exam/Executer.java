package edu.mgkit.exam;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class Executer {
    static Map<String, String> required_results = Map.of();
    String last;
    static CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet;
    HttpPost httpPost;
    StringBuilder url = new StringBuilder();
    String json;
    JsonObject obj;

    public int executeRequest(Operator op, String type, int it, String image){
        Request meth = op.getMethod(type, it);
        switch (meth.type) {
            case "NEXT":
                obj = JsonParser.parseString(required_results.get(last)).getAsJsonObject();
                ArrayList<String> links = new ArrayList<>();
                System.out.println("next");
                int k = obj.getAsJsonArray().size();
                for (int i = 0; i < k; i++) {
                    obj = obj.getAsJsonArray().get(i).getAsJsonObject();
                    for (Path a : op.getPath_to_url()) {
                        if (a.column == -1)
                            obj = obj.get(a.field).getAsJsonObject();
                        else
                            obj = obj.get(a.field).getAsJsonArray().get(a.column).getAsJsonObject();
                    }
                    String s = obj.getAsString();
                    links.add(s);
                }
                op.setLinks(links);
                required_results.clear();
                return 1;
            case "END":
                System.out.println("Next photo");
                String code = required_results.get(last);
                required_results.clear();
                if (code==op.getSuccess()) return 1;
                return -1;
            case "GET":
                url = new StringBuilder(meth.link);
                for (String a : meth.required_params) {
                    url.append('&');
                    url.append(a);
                    url.append('=');
                    url.append(required_results.get(a));
                }
                httpGet = new HttpGet(url.toString());
                CloseableHttpResponse response = null;
                try {
                    response = httpClient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    response.close();
                    json = EntityUtils.toString(entity);
                    for (Param param : meth.results)
                        param.execute(json, this);
                    executeRequest(op, type, it + 1, image);
                } catch (IOException e) {
                    return 3;
                }
                break;
            case "POST":
                if (op.hasPost())
                url = new StringBuilder(meth.link);
                else url = new StringBuilder(required_results.get(meth.link));
                for (String a : meth.required_params) {
                    url.append('&');
                    url.append(a);
                    url.append('=');
                    url.append(required_results.get(a));
                }
                httpPost = new HttpPost(url.toString());
                String tDir = System.getProperty("java.io.tmpdir");
                String path2 = tDir + "tmp" + ".jpg";
                File file = new File(path2);
                try {
                    FileUtils.copyURLToFile(new URL(image), file);
                    MultipartEntityBuilder mpeBuilder = MultipartEntityBuilder.create();
                    mpeBuilder.addBinaryBody("photo", file);
                    httpPost.setEntity(mpeBuilder.build());
                    CloseableHttpResponse response2 = null;
                    try {
                        response2 = httpClient.execute(httpPost);
                        response2.close();
                        HttpEntity entity2 = response2.getEntity();
                        json = EntityUtils.toString(entity2);
                        for (Param param : meth.results)
                            param.execute(json, this);
                        file.delete();
                        executeRequest(op, type, it + 1, image);
                    } catch (IOException e) {
                        return 4;
                    }
                } catch (IOException e) {
                    return 5;
                }
                break;
        }
        return -1;
    }

    public void setAccess(Operator op)
    {
        required_results.clear();
        String[] access = op.getAccess_params();
        Map<String,String> results = op.getAccess_results();
        for (String a:access)
            required_results.put(a,results.get(a));
    }
}
