package Models;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
//Class containing data parsed from Trivia APIs JSON, allows us to alter and display it.
public class Result {
	private String category;

	private String correct_answer;

	private String difficulty;

	private String question;

	private String type;

	private List<String> incorrect_answers;

	public String getCategory() throws UnsupportedEncodingException {
		String result = java.net.URLDecoder.decode(category, "UTF-8"); //URL decoding into UTF-8 to avoid messed up strings
																	   //and to match encoding of API call.
		return result;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCorrect_answer() throws UnsupportedEncodingException {
		String result = java.net.URLDecoder.decode(correct_answer, "UTF-8");

		return result;
	}

	public void setCorrect_answer(String correct_answer) {
		this.correct_answer = correct_answer;
	}

	public String getDifficulty() {

		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getQuestion() throws UnsupportedEncodingException {
		String result = java.net.URLDecoder.decode(question, "UTF-8");

		return result;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getIncorrect_answers() throws UnsupportedEncodingException {
		List<String> encoded_answers = new ArrayList<String>();

		for (String elements : incorrect_answers) {
			encoded_answers.add(java.net.URLDecoder.decode(elements, "UTF-8"));
		}
		return encoded_answers;
	}

	public void setIncorrect_answers(List<String> incorrect_answers) {
		this.incorrect_answers = incorrect_answers;
	}

	@Override
	public String toString() {
		return "ClassPojo [category = " + category + ", correct_answer = " + correct_answer + ", difficulty = "
				+ difficulty + ", question = " + question + ", type = " + type + ", incorrect_answers = "
				+ incorrect_answers + "]";
	}

}
