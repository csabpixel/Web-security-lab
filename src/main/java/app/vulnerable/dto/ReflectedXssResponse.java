package app.vulnerable.dto;

public class ReflectedXssResponse {

    private String input;
    private String mode;
    private String message;

    public ReflectedXssResponse() {
    }

    public ReflectedXssResponse(String input, String mode, String message) {
        this.input = input;
        this.mode = mode;
        this.message = message;
    }

    public String getInput() {
        return input;
    }

    public String getMode() {
        return mode;
    }

    public String getMessage() {
        return message;
    }
}