package Models;

//Skeleton class containing a check to see whether or not the question was answered correctly, 
//the question's point value, and the correct answer.
public class CorrectCheck {
	private int points;
	private boolean ifCorrect;
	private String correctAns;

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public boolean getifCorrect() {
		return ifCorrect;
	}

	public void setifCorrect(boolean ifCorrect) {
		this.ifCorrect = ifCorrect;
	}

	public String getcorrectAns() {
		return correctAns;
	}

	public void setcorrectAns(String correctAns) {
		this.correctAns = correctAns;
	}
}
