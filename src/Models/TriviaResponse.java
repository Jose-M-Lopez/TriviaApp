package Models;

import java.util.List;

//Class that stores JSON API results, needed to parse.
public class TriviaResponse {
    private List<Result> results;
    private String response_code;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getResponse_code() {
        return response_code;
    }

    public void setResponse_code(String response_code) {
        this.response_code = response_code;
    }

    @Override
    public String toString() {
        return "ClassPojo [results = " + results + ", response_code = " + response_code + "]";
    }
}