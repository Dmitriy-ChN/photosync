package edu.mgkit.exam;

import com.google.gson.JsonArray;
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
import java.util.HashMap;

public class Executer {
    HashMap<String, String> required_results = new HashMap<>();
    String last;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet;
    HttpPost httpPost;
    StringBuilder url = new StringBuilder();
    String json;
    JsonObject obj;

    public int executeRequest(Operator op, String type, int it, String image){
        Request meth = op.getMethod(type, it);
        System.out.println(meth.link);
        switch (meth.type) {
            case "NEXT":
                JsonObject obj;
                JsonArray arr = JsonParser.parseString(required_results.get(last)).getAsJsonArray();
                String res;
                ArrayList<String> links = new ArrayList<>();
                System.out.println("next");
                int k = arr.size();
                for (int i = 0; i < k; i++) {
                    res = arr.get(i).toString();
                    for (Path a : op.getPath_to_url()) {
                        obj = JsonParser.parseString(res).getAsJsonObject();
                        if (a.column!=-1)
                            if (a.column==-2)
                                res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                            else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                        else
                            res = obj.get(a.field).toString();
                    }
                    links.add(res);
                    System.out.println(res);
                }
                op.setLinks(links);
                required_results.clear();
                return 1;
            case "END":
                System.out.println("Next photo");
                String code = required_results.get(last);
                required_results.clear();
                if (code.equals(op.getSuccess())) return 1;
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
                try {
                    CloseableHttpResponse response = httpClient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    json = EntityUtils.toString(entity);
                    response.close();
                    System.out.println(json);
                    for (Param param : meth.results)
                        param.execute(json, this);
                    return executeRequest(op, type, it + 1, image);
                } catch (IOException e) {
                    e.printStackTrace();
                    return 3;
                }
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
                    try {
                        CloseableHttpResponse response2 = httpClient.execute(httpPost);
                        response2.close();
                        HttpEntity entity2 = response2.getEntity();
                        json = EntityUtils.toString(entity2);
                        for (Param param : meth.results)
                            param.execute(json, this);
                        file.delete();
                        return executeRequest(op, type, it + 1, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 4;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return 5;
                }
        }
        return -1;
    }

    public void setAccess(Operator op)
    {
        required_results.clear();
        String[] access = op.getAccess_params();
        HashMap<String,String> results = op.getAccess_results();
        for (String a:access)
            required_results.put(a,results.get(a));
    }

    public void setLast(String last_) {last = last_;}
}
