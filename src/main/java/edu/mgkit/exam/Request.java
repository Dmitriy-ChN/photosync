package edu.mgkit.exam;

public class Request
{
    String type;
    Path[] required_params;
    String link;
    Param[] results;
    Param error;
    Param error_message;

    public Request(String type, Path[] required_params, String link, Param[] results, Param error, Param error_message) {
        this.type = type;
        this.required_params = required_params;
        this.link = link;
        this.results = results;
        this.error = error;
        this.error_message = error_message;
    }

    public Request(String type) {
        this.type = type;
    }
}

