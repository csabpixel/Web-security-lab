package app.vulnerable.dto;

public class BlindTimeResponse {

    private String mode;
    private String input;
    private String message;
    private long responseTimeMs;
    private String constructedSql;

    public BlindTimeResponse() {
    }

    public BlindTimeResponse(String mode, String input, String message, long responseTimeMs, String constructedSql) {
        this.mode = mode;
        this.input = input;
        this.message = message;
        this.responseTimeMs = responseTimeMs;
        this.constructedSql = constructedSql;
    }

    public String getMode() {
        return mode;
    }

    public String getInput() {
        return input;
    }

    public String getMessage() {
        return message;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public String getConstructedSql() {
        return constructedSql;
    }
}