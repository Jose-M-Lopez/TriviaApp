package Models;

import java.util.ArrayList;
import java.util.List;
//Class to insert and store all the rounds for a specific player.
public class RoundHistory {
	int LifeTimePoints;
	//List containing all the rounds for a given player.
	List<QuestionHistory> roundList = new ArrayList<QuestionHistory>();

	public void setPoints(int pointsEarned) {
		this.LifeTimePoints = pointsEarned;
	}

	public int getPoints() {
		return LifeTimePoints;
	}
	//Insert new round into players round history.
	public void insertQuestionHistory(QuestionHistory questionHistory) {
		roundList.add(questionHistory);
	}
}
