package edu.mgkit.exam;

public class Request
{
    String type;
    String[] required_params;
    String link;
    Param[] results;

    public Request(String type, String[] required_params, String link, Param[] results) {
        this.type = type;
        this.required_params = required_params;
        this.link = link;
        this.results = results;
    }
}
