package app.vulnerable.dto;

public class BlindTimeRequest {

    private String input;
    private String mode;

    public BlindTimeRequest() {
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