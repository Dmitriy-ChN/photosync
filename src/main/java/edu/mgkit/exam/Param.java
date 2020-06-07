package edu.mgkit.exam;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Param
{
    String name;
    Boolean isConcatenated;
    String formatString;
    ArrayList<Path>[] path_to_params;

    public Param(String name, Boolean isConcatenated, String formatString, ArrayList<Path>[] path_to_params) {
        this.name = name;
        this.isConcatenated = isConcatenated;
        this.formatString = formatString;
        this.path_to_params = path_to_params;
    }

    private String interpolation (String s, String[] args)
    {
        StringBuilder res = new StringBuilder();
        int q = 0;
        for (String a:args)
        {
            res.append(s, q, s.indexOf('%', q)).append(a);
            q = s.indexOf('%',q)+2;
        }
        return res.toString();
    }
    private String execute(String json)
    {
        if (!isConcatenated)
        {
            JsonObject obj;
            String res = json;
            for (int i = 0; i< path_to_params[0].size()-1; i++)
            {
                Path a = path_to_params[0].get(i);
                obj = JsonParser.parseString(res).getAsJsonObject();
                if (a.column!=-1)
                    if (a.column==-2)

                    res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                    else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                else
                    res = obj.get(a.field).toString();

            }
            if (res.startsWith("\"[")) res = JsonParser.parseString(res).getAsJsonArray().get(path_to_params[0].get(path_to_params[0].size()-2).column).getAsString();
            else res = JsonParser.parseString(res).getAsJsonObject().get(path_to_params[0].get(path_to_params[0].size()-1).field).getAsString();
            System.out.println(res);
            return res;
        }
        else
        {
            String[] params = new String[path_to_params.length];
            for (int j = 0; j < params.length; j++)
            {
                JsonObject obj;
                String res = json;
                for (int i = 0; i< path_to_params[j].size()-1; i++)
                {
                    Path a = path_to_params[j].get(i);
                    obj = JsonParser.parseString(res).getAsJsonObject();
                    if (a.column!=-1)
                        if (a.column==-2)

                            res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                        else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                    else
                        res = obj.get(a.field).getAsJsonObject().toString();

                }
                if (res.startsWith("\"[")) res = JsonParser.parseString(res).getAsJsonArray().get(path_to_params[j].get(path_to_params[j].size()-2).column).getAsString();
                else res = JsonParser.parseString(res).getAsJsonObject().get(path_to_params[j].get(path_to_params[j].size()-1).field).getAsString();
                System.out.println(res);
                params[j] = res;
            }
            return interpolation(formatString,params);
        }
    }

    public void addParam(String json, Executer exec)
    {
        exec.required_results.put(name,execute(json));
        exec.setLast(name);
    }

    public boolean checkError(String json, Request cur)
    {
        return (execute(json).equals(cur.error_code));
    }
}
