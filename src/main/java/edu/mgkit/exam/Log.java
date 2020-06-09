package edu.mgkit.exam;

public class Log {
    private String image;
    private String result;
    private String message;
    private int index;

    public String getImage() {
        return image;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getIndex() {return String.valueOf(index);}

    public Log(int index, String image, String result, String message) {
        this.index = index;
        this.image = image;
        this.result = result;
        this.message = message;
    }
}
