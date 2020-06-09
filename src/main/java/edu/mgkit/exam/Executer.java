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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    String error;

    public int executeRequest(Operator op, String type, int it, String image){
        error = "";
        Request meth = op.getMethod(type, it);
        System.out.println(meth.link);
        switch (meth.type) {
            case "NEXT":
                System.out.println(required_results.get(last));
                JsonArray arr = JsonParser.parseString(required_results.get(last)).getAsJsonArray();
                String res;
                ArrayList<String> links = new ArrayList<>();
                System.out.println("next");
                int k = arr.size();
                for (int i = 0; i < k; i++)
                {
                    res = arr.get(i).toString();
                    for (int j = 0; j< op.getPath_to_url().length-1; j++)
                {
                    Path a = op.getPath_to_url()[j];
                    obj = JsonParser.parseString(res).getAsJsonObject();
                    if (a.column!=-1)
                        if (a.column==-2)

                            res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                        else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                    else
                        res = obj.get(a.field).getAsJsonObject().toString();

                }
                if (res.startsWith("\"[")) res = JsonParser.parseString(res).getAsJsonArray().get(op.getPath_to_url()[op.getPath_to_url().length-2].column).getAsString();
                else res = JsonParser.parseString(res).getAsJsonObject().get(op.getPath_to_url()[op.getPath_to_url().length-1].field).getAsString();

                    System.out.println(res);
                    links.add(res);
                }
                op.setLinks(links);
                System.out.println(links.toString());
                return 1;
            case "END":
                System.out.println("Next photo");
                String code = required_results.get(last);
                if (code.equals(op.getSuccess())) return 1;
                return -1;
            case "GET":
                url = new StringBuilder(meth.link);
                for (Path a : meth.required_params) {
                    url.append('&');
                    url.append(a.field);
                    url.append('=');
                    url.append(URLEncoder.encode(required_results.get(a.field),StandardCharsets.UTF_8));
                    System.out.println(url.toString());
                }
                httpGet = new HttpGet(url.toString());
                try {
                    CloseableHttpResponse response = httpClient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    json = EntityUtils.toString(entity);
                    System.out.println(json);
                    if (meth.error.checkError(json))
                    {
                        if (meth.error_message!=null) {
                            meth.error_message.addParam(json, this);
                            error = required_results.get(last);
                        }
                        return -2;
                    }
                        for (Param param : meth.results)
                        param.addParam(json, this);
                    return executeRequest(op, type, it + 1, image);
                } catch (Exception e) {
                    error = "inner error";
                    e.printStackTrace();
                    return 3;
                }
            case "POST":
                if (op.hasPost())
                url = new StringBuilder(meth.link);
                else url = new StringBuilder(required_results.get(meth.link));
                if (url.toString().startsWith((String.valueOf((char)34)))) url = new StringBuilder(url.toString().substring(1,url.toString().length()-1));
                System.out.println(url.toString());
                MultipartEntityBuilder mpeBuilder = MultipartEntityBuilder.create();
                required_results.put(op.getImageName(),image);
                try {
                for (Path a : meth.required_params) {
                    if (a.column==0)
                    {
                        url.append('&');
                        url.append(a.field);
                        url.append('=');
                        url.append(URLEncoder.encode(required_results.get(a.field), StandardCharsets.UTF_8));
                    }
                    else if (a.column==1)
                    {
                        mpeBuilder.addTextBody(a.field,required_results.get(a.field));
                    }
                    else if (a.column==2)
                    {
                        String tDir = System.getProperty("java.io.tmpdir");
                        String path2 = tDir + "tmp" + ".jpg";
                        File file = new File(path2);
                        file.deleteOnExit();
                        FileUtils.copyURLToFile(new URL(required_results.get(a.field)), file);
                        mpeBuilder.addBinaryBody(a.field, file);
                    }
                }
                System.out.println(url.toString());
                httpPost = new HttpPost(url.toString());
                    httpPost.setEntity(mpeBuilder.build());
                    try {
                        CloseableHttpResponse response2 = httpClient.execute(httpPost);
                        HttpEntity entity2 = response2.getEntity();
                        json = EntityUtils.toString(entity2);
                        if (meth.error.checkError(json))
                        {
                            if (meth.error_message!=null) {
                                meth.error_message.addParam(json, this);
                                error = required_results.get(last);
                            }
                            return -2;
                        }
                            for (Param param : meth.results)
                                param.addParam(json, this);
                            return executeRequest(op, type, it + 1, image);
                    } catch (Exception e) {
                        error = "inner error";
                        e.printStackTrace();
                        return 4;
                    }
                } catch (Exception e) {
                    error = "inner error";
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

    public String getError() {return error;}
}
