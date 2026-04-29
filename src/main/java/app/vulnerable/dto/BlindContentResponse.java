package app.vulnerable.dto;

public class BlindContentResponse {

    private String mode;
    private String input;
    private boolean success;
    private String message;

    public BlindContentResponse() {
    }

    public BlindContentResponse(String mode, String input, boolean success, String message) {
        this.mode = mode;
        this.input = input;
        this.success = success;
        this.message = message;
    }

    public String getMode() {
        return mode;
    }

    public String getInput() {
        return input;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}