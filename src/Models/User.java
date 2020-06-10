package Models;
//Class that stores user information.
public class User {
	private String username;
	private String password;
	private int ID;
	private int points;

	public User(String username, String password, int ID) {
		this.username = username;
		this.password = password;
		this.ID = ID;
	}

	public User(String username, int points) {
		this.username = username;
		this.points = points;

	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setHash(String password) {
		this.password = password;
	}

	public String getHash() {
		return password;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}
