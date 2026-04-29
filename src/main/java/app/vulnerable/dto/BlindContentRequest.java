package app.vulnerable.dto;

public class BlindContentRequest {

    private String input;
    private String mode;

    public BlindContentRequest() {
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