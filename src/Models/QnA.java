package Models;

import java.util.List;

//Class that contains a question,its position in a questionList, and all of its potential answers.
public class QnA {
    private List<String> answers;
    private String question;
    private int counter;

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}
