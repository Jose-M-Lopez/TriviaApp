package Models;

//Skeleton class that contains each individual question in question history.
public class Question {
	private boolean ifCorrect;
	private String question;
	private String difficulty;
	private String category;

	public Question(String question, String difficulty, String category, boolean ifCorrect) {
		this.question = question;
		this.difficulty = difficulty;
		this.category = category;
		this.ifCorrect = ifCorrect;
	}

}
