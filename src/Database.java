import Models.Leaderboard;
import Models.Question;
import Models.QuestionHistory;
import Models.RoundHistory;
import Models.User;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

public class Database {
	DataSource datasource;
	
	@Inject
	public Database(DataSource ds) throws SQLException
	{
		datasource = ds;
	}

	// Passing query into database to insert questions.
	void insertQuestion(String difficulty, String category, String question, boolean ifCorrect, int roundID)
			throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String insertionQuery = "INSERT INTO question (Difficulty, Category, Question, ifCorrect, RoundID)"
				+ " VALUES (?, ?, ?, ?, ?);";
		
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(insertionQuery);
			statement.setString(1, difficulty);
			statement.setString(2, category);
			statement.setString(3, question);
			statement.setBoolean(4, ifCorrect);
			statement.setInt(5, roundID);
			statement.executeUpdate();
		// Checking and closing statement/connection.
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Passing query into database to insert rounds.
	int insertRound(int UserID) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String insertionQuery = "INSERT INTO round (UserID)" + " VALUES (?);";

		try {
			conn = datasource.getConnection();
			// Statement.RETURN_GENERATED_KEYS allows for generated key values such as
			// RoundID, to be returned.
			statement = conn.prepareStatement(insertionQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, UserID);
			statement.executeUpdate();
			// Storing returned generated keys in Result.
			ResultSet Result = statement.getGeneratedKeys();
			Result.first();
			int key = Result.getInt(1);
			// returning RoundID.
			return key;
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Passing query into database to update a rounds points after a round has
	// ended.
	void updateRound(int Points, int RoundID) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String updateQuery = "UPDATE round SET Points_Earned = ? WHERE RoundID = ?";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(updateQuery);
			statement.setInt(1, Points);
			statement.setInt(2, RoundID);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Passing query into database to create new user.
	void createUser(String Username, String Password) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String insertionQuery = "INSERT INTO user (Username, Password)" + " VALUES (?, ?);";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(insertionQuery);
			statement.setString(1, Username);
			statement.setString(2, Password);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Returning User object that contains user information from Database.
	User getUser(String Username) throws SQLException {
		PreparedStatement statement = null;
		PreparedStatement checkStatement = null;
		Connection conn = null;
		String selectCount = "SELECT COUNT(UserID) FROM user WHERE Username = ?";

		try {
			conn = datasource.getConnection();
			checkStatement = conn.prepareStatement(selectCount);
			checkStatement.setString(1, Username);
			ResultSet checkRS = checkStatement.executeQuery();
			checkRS.first();
			// Returning null if user does not exist within the database.
			if (checkRS.getInt(1) == 0) {
				return null;
			}
			// Storing user information into getUser object if they exist.
			String selectQuery = "SELECT * FROM user WHERE Username = ?";
			statement = conn.prepareStatement(selectQuery);
			statement.setString(1, Username);
			ResultSet rs = statement.executeQuery();
			rs.first();
			User player = new User(rs.getString("Username"), rs.getString("Password"), rs.getInt("UserID"));
			return player;

		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Updating total points for a user after a round has ended.
	void updateTotalPoints(int points, int userID) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String updateQuery = "UPDATE user SET Total_Points = Total_Points + ? WHERE UserID = ?";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(updateQuery);
			statement.setInt(1, points);
			statement.setInt(2, userID);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Removing rounds with a null point value(incompleted rounds).
	void deleteIncompleteRounds() throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String deleteQuery = "DELETE FROM round WHERE Points_Earned IS NULL";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(deleteQuery);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Removing questions associated with an incomplete round.
	void deleteIncompleteQuestions() throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		String deleteQuery = "DELETE FROM `question` WHERE RoundID IN"
				+ "(SELECT RoundID FROM round WHERE Points_earned IS NULL)";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(deleteQuery);
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	// Returning top ten players with the greatest point values.
	Leaderboard getTopTenPlayers() throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		Leaderboard topPlayers = new Leaderboard();
		String selectQuery = "SELECT * FROM user ORDER BY Total_Points DESC LIMIT 10";
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(selectQuery);
			ResultSet rs = statement.executeQuery();
			// Storing top users into a leaderboard object to hold them.
			while (rs.next()) {
				User player = new User(rs.getString("Username"), rs.getInt("Total_Points"));
				topPlayers.insertUser(player);
			}
			return topPlayers;
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Returning the questions of a specific round for a specific user.
	QuestionHistory getQuestionHistory(int RoundID, int UserID) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		QuestionHistory questions = new QuestionHistory(RoundID, UserID);
		String questionQuery = "SELECT * FROM `question` WHERE RoundID = ?";

		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(questionQuery);
			statement.setInt(1, RoundID);
			ResultSet rs = statement.executeQuery();
			// While there are still more rows.
			while (rs.next()) {
				// Inserting question information into question object, which then gets inserted
				// into a QuestionHistory object.
				Question question = new Question(rs.getString("Question"), rs.getString("Difficulty"),
						rs.getString("Category"), rs.getBoolean("ifCorrect"));
				questions.insertQuestion(question);
			}

			return questions;
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}

	// Returning the rounds for a specific user with their respective
	// QuestionHistory.
	RoundHistory getRoundHistory(int UserID) throws SQLException {
		PreparedStatement statement = null;
		Connection conn = null;
		RoundHistory rounds = new RoundHistory();
		int total_points = 0;
		String questionQuery = "SELECT * FROM `round` WHERE UserID = ?";

		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(questionQuery);
			statement.setInt(1, UserID);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				// Inserting QuestionHistories into a RoundHistory object.
				total_points += rs.getInt("Points_Earned");
				QuestionHistory questions = new QuestionHistory(rs.getInt("RoundID"), rs.getInt("Points_Earned"));
				rounds.insertQuestionHistory(questions);
			}
			rounds.setPoints(total_points);
			return rounds;
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

	}
}
