package edu.mgkit.exam;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    public void execute(String json, Executer exec)
    {
        if (!isConcatenated)
        {
            JsonObject obj;
            String res = json;
            for (Path a:path_to_params[0])
            {

                obj = JsonParser.parseString(res).getAsJsonObject();
                if (a.column!=-1)
                    if (a.column==-2)

                    res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                    else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                else
                    res = obj.get(a.field).toString();

            }
            System.out.println(res);
            exec.required_results.put(name,res);
        }
        else
        {
            String[] params = new String[path_to_params.length];
            for (int i = 0; i < params.length; i++)
            {
                JsonObject obj;
                String res = json;
                for (Path a:path_to_params[0])
                {

                    obj = JsonParser.parseString(res).getAsJsonObject();
                    if (a.column!=-1)
                        if (a.column==-2)

                            res = obj.get(a.field).getAsJsonArray().get(obj.get(a.field).getAsJsonArray().size()-1).toString();
                        else res = obj.get(a.field).getAsJsonArray().get(a.column).toString();
                    else
                        res = obj.get(a.field).toString();

                }
                System.out.println(res);
                params[i] = res;
            }
            exec.required_results.put(name,interpolation(formatString,params));
        }
        exec.setLast(name);
    }
}
