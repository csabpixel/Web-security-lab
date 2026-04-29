package app.vulnerable.dto;

public class ReflectedXssRequest {

    private String input;
    private String mode;

    public ReflectedXssRequest() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}