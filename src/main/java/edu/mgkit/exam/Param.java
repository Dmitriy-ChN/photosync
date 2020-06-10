package edu.mgkit.exam;

import com.google.gson.JsonElement;
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
    private String execute(String json)
    {
        if (!isConcatenated)
        {
            JsonElement obj;
            String res = json;
            System.out.println(res);
            System.out.println("1");
            for (Path a:path_to_params[0])
            {
                obj = JsonParser.parseString(res);
                if (a.field.equals("empty"))
                {
                    if (a.column != -1)
                        if (a.column == -2)

                            res = String.valueOf(obj.getAsJsonArray().get(obj.getAsJsonArray().size() - 1));
                        else res = String.valueOf(obj.getAsJsonArray().get(a.column));
                    else
                        res = String.valueOf(obj);
                }
                else {
                    if (a.column != -1)
                        if (a.column == -2)

                            res = String.valueOf(obj.getAsJsonObject().get(a.field).getAsJsonArray().get(obj.getAsJsonObject().get(a.field).getAsJsonArray().size() - 1));
                        else res = String.valueOf(obj.getAsJsonObject().get(a.field).getAsJsonArray().get(a.column));
                    else
                        res = String.valueOf(obj.getAsJsonObject().get(a.field));
                }
                System.out.println(res);
            }
            if (res.startsWith(String.valueOf((char)34))) res = JsonParser.parseString(res).getAsString();
            System.out.println(res);
            System.out.println("2");
            return res;
        }
        else
        {
            String[] params = new String[path_to_params.length];
            for (int j = 0; j < params.length; j++)
            {
                JsonElement obj;
                String res = json;
                for (Path a:path_to_params[j])
                {
                    obj = JsonParser.parseString(res);
                    if (a.field.equals("empty"))
                    {
                        if (a.column != -1)
                            if (a.column == -2)

                                res = String.valueOf(obj.getAsJsonArray().get(obj.getAsJsonArray().size() - 1));
                            else res = String.valueOf(obj.getAsJsonArray().get(a.column));
                        else
                            res = String.valueOf(obj);
                    }
                    else {
                        if (a.column != -1)
                            if (a.column == -2)

                                res = String.valueOf(obj.getAsJsonObject().get(a.field).getAsJsonArray().get(obj.getAsJsonObject().get(a.field).getAsJsonArray().size() - 1));
                            else res = String.valueOf(obj.getAsJsonObject().get(a.field).getAsJsonArray().get(a.column));
                        else
                            res = String.valueOf(obj.getAsJsonObject().get(a.field));
                    }
                    System.out.println(res);

                }
                if (res.startsWith(String.valueOf((char)34))) res = JsonParser.parseString(res).getAsString();
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

    public boolean checkError(String json)
    {
        return (!execute(json).equals("null") && execute(json).equals(name));
    }
}
