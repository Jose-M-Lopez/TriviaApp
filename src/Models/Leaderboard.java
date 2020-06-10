package Models;

import java.util.ArrayList;
import java.util.List;

//Skeleton class to hold top players for leaderboard display.
public class Leaderboard {
	private List<User> topPlayers = new ArrayList<User>();

	public List<User> getTopPlayers() {
		return topPlayers;
	}
	//Add player to leaderboard.
	public void insertUser(User player) {
		topPlayers.add(player);
	}

}
