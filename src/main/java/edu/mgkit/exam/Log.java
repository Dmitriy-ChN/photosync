package edu.mgkit.exam;

public class Log {
    private String image;
    private String result;
    private String message;

    public String getImage() {
        return image;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Log(String image, String result, String message) {
        this.image = image;
        this.result = result;
        this.message = message;
    }
}
