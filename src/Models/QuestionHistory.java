package Models;

import java.util.ArrayList;
import java.util.List;
//Class to insert and store all the questions for a specific round.
public class QuestionHistory {
	int totalPoints;
	int RoundID;

	public QuestionHistory(int RoundID, int totalPoints) {
		this.RoundID = RoundID;
		this.totalPoints = totalPoints;
	}
	//List containing all the questions for the given round.
	private List<Question> roundQuestions = new ArrayList<Question>();

	public List<Question> getQuestion() {
		return roundQuestions;
	}
	//Add a new question to the round.			
	public void insertQuestion(Question question) {
		roundQuestions.add(question);
	}

	public void setPoints(int points) {
		totalPoints = points;
	}

	public void setRoundID(int RoundID) {
		this.RoundID = RoundID;
	}

}
