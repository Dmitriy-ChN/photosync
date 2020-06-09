package edu.mgkit.exam;

public class Request
{
    String type;
    Path[] required_params;
    String link;
    Param[] results;
    String error_code;
    Param error_path;
    Param error_message;

    public Request(String type, Path[] required_params, String link, Param[] results, String error_code, Param error_path, Param error_message) {
        this.type = type;
        this.required_params = required_params;
        this.link = link;
        this.results = results;
        this.error_code = error_code;
        this.error_path = error_path;
        this.error_message = error_message;
    }

    public Request(String type) {
        this.type = type;
    }
}

