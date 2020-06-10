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
    String lastParameter;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet;
    HttpPost httpPost;
    StringBuilder requestURL = new StringBuilder();
    String json;
    JsonObject obj;
    String error;

    public int executeRequest(Operator currentOperator, String type, int number, String imageURL){
        error = "";
        Request request = currentOperator.getRequest(type, number);
        switch (request.type) {
            case "NEXT":
                JsonArray arr = JsonParser.parseString(required_results.get(lastParameter)).getAsJsonArray();
                String result;
                ArrayList<String> links = new ArrayList<>();
                System.out.println("next");
                for (int i = 0; i < arr.size(); i++)
                {
                    result = arr.get(i).toString();
                    for (int j = 0; j< currentOperator.getPath_to_url().length-1; j++)
                {
                    Path valuePath = currentOperator.getPath_to_url()[j];
                    obj = JsonParser.parseString(result).getAsJsonObject();
                    if (valuePath.column!=-1)
                        if (valuePath.column==-2)

                            result = obj.get(valuePath.field).getAsJsonArray().get(obj.get(valuePath.field).getAsJsonArray().size()-1).toString();
                        else result = obj.get(valuePath.field).getAsJsonArray().get(valuePath.column).toString();
                    else
                        result = obj.get(valuePath.field).getAsJsonObject().toString();

                }
                if (result.startsWith("\"[")) result = JsonParser.parseString(result).getAsJsonArray().get(currentOperator.getPath_to_url()[currentOperator.getPath_to_url().length-2].column).getAsString();
                else result = JsonParser.parseString(result).getAsJsonObject().get(currentOperator.getPath_to_url()[currentOperator.getPath_to_url().length-1].field).getAsString();

                    links.add(result);
                }
                currentOperator.setLinks(links);
                return 1;
            case "END":
                String code = required_results.get(lastParameter);
                if (code.equals(currentOperator.getSuccess())) return 1;
                return -1;
            case "GET":
                requestURL = new StringBuilder(request.link);
                for (Path a : request.required_params) {
                    requestURL.append('&');
                    requestURL.append(a.field);
                    requestURL.append('=');
                    requestURL.append(URLEncoder.encode(required_results.get(a.field),StandardCharsets.UTF_8));
                    System.out.println(requestURL.toString());
                }
                httpGet = new HttpGet(requestURL.toString());
                try {
                    CloseableHttpResponse response = httpClient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    json = JsonParser.parseString(EntityUtils.toString(entity)).toString();
                    System.out.println(json);
                    if (request.error.checkError(json))
                    {
                        if (request.error_message!=null) {
                            request.error_message.addParam(json, this);
                            error = required_results.get(lastParameter);
                        }
                        return -2;
                    }
                        for (Param parameter : request.results)
                        parameter.addParam(json, this);
                    return executeRequest(currentOperator, type, number + 1, imageURL);
                } catch (Exception e) {
                    error = "inner error";
                    e.printStackTrace();
                    return 3;
                }
            case "POST":
                if (currentOperator.hasPost())
                requestURL = new StringBuilder(request.link);
                else requestURL = new StringBuilder(required_results.get(request.link));
                if (requestURL.toString().startsWith((String.valueOf((char)34)))) requestURL = new StringBuilder(requestURL.toString().substring(1, requestURL.toString().length()-1));
                MultipartEntityBuilder mpeBuilder = MultipartEntityBuilder.create();
                required_results.put(currentOperator.getImageName(),imageURL);
                try {
                for (Path parameter : request.required_params) {
                    if (parameter.column==0)
                    {
                        requestURL.append('&');
                        requestURL.append(parameter.field);
                        requestURL.append('=');
                        requestURL.append(URLEncoder.encode(required_results.get(parameter.field), StandardCharsets.UTF_8));
                    }
                    else if (parameter.column==1)
                    {
                        mpeBuilder.addTextBody(parameter.field,required_results.get(parameter.field));
                    }
                    else if (parameter.column==2)
                    {
                        String tDir = System.getProperty("java.io.tmpdir");
                        String path2 = tDir + "tmp" + ".jpg";
                        File file = new File(path2);
                        file.deleteOnExit();
                        FileUtils.copyURLToFile(new URL(required_results.get(parameter.field)), file);
                        mpeBuilder.addBinaryBody(parameter.field, file);
                    }
                }
                httpPost = new HttpPost(requestURL.toString());
                    httpPost.setEntity(mpeBuilder.build());
                    try {
                        CloseableHttpResponse response2 = httpClient.execute(httpPost);
                        HttpEntity entity2 = response2.getEntity();
                        json = EntityUtils.toString(entity2);
                        if (request.error.checkError(json))
                        {
                            if (request.error_message!=null) {
                                request.error_message.addParam(json, this);
                                error = required_results.get(lastParameter);
                            }
                            return -2;
                        }
                            for (Param param : request.results)
                                param.addParam(json, this);
                            return executeRequest(currentOperator, type, number + 1, imageURL);
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

    public void setLastParameter(String last_) {
        lastParameter = last_;}

    public String getError() {return error;}
}
