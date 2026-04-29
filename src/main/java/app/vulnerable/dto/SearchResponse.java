package app.vulnerable.dto;

import java.util.List;
import java.util.Map;

public class SearchResponse {

    private String mode;
    private String input;
    private String explanation;
    private List<Map<String, Object>> results;

    public SearchResponse() {
    }

    public SearchResponse(String mode, String input, String explanation, List<Map<String, Object>> results) {
        this.mode = mode;
        this.input = input;
        this.explanation = explanation;
        this.results = results;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }
}