import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
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

public class Database {
	private Connection conn;

	public Database() throws SQLException {
		// Establishing connection to local database.
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setDatabaseName("Trivia");
		dataSource.setUser("root");
		dataSource.setPassword("");
		dataSource.setServerName("localhost");

		conn = dataSource.getConnection();
	}

	// Passing query into database to insert questions.
	void insertQuestion(String difficulty, String category, String question, boolean ifCorrect, int roundID)
			throws SQLException {
		String insertionQuery = "INSERT INTO question (Difficulty, Category, Question, ifCorrect, RoundID)"
				+ " VALUES (?, ?, ?, ?, ?);";
		PreparedStatement statement = conn.prepareStatement(insertionQuery);
		statement.setString(1, difficulty);
		statement.setString(2, category);
		statement.setString(3, question);
		statement.setBoolean(4, ifCorrect);
		statement.setInt(5, roundID);
		statement.executeUpdate();
	}

	// Passing query into database to insert rounds.
	int insertRound(int UserID) throws SQLException {
		String insertionQuery = "INSERT INTO round (UserID)" + " VALUES (?);";
		// Statement.RETURN_GENERATED_KEYS allows for generated key values such as
		// RoundID, to be returned.
		PreparedStatement statement = conn.prepareStatement(insertionQuery, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, UserID);
		statement.executeUpdate();
		// Storing returned generated keys in Result.
		ResultSet Result = statement.getGeneratedKeys();
		Result.first();
		int key = Result.getInt(1);
		// returning RoundID.
		return key;
	}

	// Passing query into database to update a rounds points after a round has
	// ended.
	void updateRound(int Points, int RoundID) throws SQLException {
		String updateQuery = "UPDATE round SET Points_Earned = ? WHERE RoundID = ?";
		PreparedStatement statement = conn.prepareStatement(updateQuery);
		statement.setInt(1, Points);
		statement.setInt(2, RoundID);
		statement.executeUpdate();
	}

	// Passing query into database to create new user.
	void createUser(String Username, String Password) throws SQLException {
		String insertionQuery = "INSERT INTO user (Username, Password)" + " VALUES (?, ?);";
		PreparedStatement statement = conn.prepareStatement(insertionQuery);
		statement.setString(1, Username);
		statement.setString(2, Password);
		statement.executeUpdate();
	}

	// Returning User object that contains user information from Database.
	User getUser(String Username) throws SQLException {
		String selectCount = "SELECT COUNT(UserID) FROM user WHERE Username = ?";
		PreparedStatement checkStatement = conn.prepareStatement(selectCount);
		checkStatement.setString(1, Username);
		ResultSet checkRS = checkStatement.executeQuery();
		checkRS.first();
		// Returning null if user does not exist within the database.
		if (checkRS.getInt(1) == 0) {
			return null;
		}
		// Storing user information into getUser object if they exist.
		String selectQuery = "SELECT * FROM user WHERE Username = ?";
		PreparedStatement statement = conn.prepareStatement(selectQuery);
		statement.setString(1, Username);
		ResultSet rs = statement.executeQuery();
		rs.first();
		User player = new User(rs.getString("Username"), rs.getString("Password"), rs.getInt("UserID"));
		return player;
	}

	// Updating total points for a user after a round has ended.
	void updateTotalPoints(int points, int userID) throws SQLException {
		String updateQuery = "UPDATE user SET Total_Points = Total_Points + ? WHERE UserID = ?";
		PreparedStatement statement = conn.prepareStatement(updateQuery);
		statement.setInt(1, points);
		statement.setInt(2, userID);
		statement.executeUpdate();
	}

	// Removing rounds with a null point value(incompleted rounds).
	void deleteIncompleteRounds() throws SQLException {
		String deleteQuery = "DELETE FROM round WHERE Points_Earned IS NULL";
		PreparedStatement statement = conn.prepareStatement(deleteQuery);
		statement.executeUpdate();
	}

	// Removing questions associated with an incomplete round.
	void deleteIncompleteQuestions() throws SQLException {
		String deleteQuery = "DELETE FROM `question` WHERE RoundID IN"
				+ "(SELECT RoundID FROM round WHERE Points_earned IS NULL)";
		PreparedStatement statement = conn.prepareStatement(deleteQuery);
		statement.executeUpdate();

	}

	// Returning top ten players with the greatest point values.
	Leaderboard getTopTenPlayers() throws SQLException {
		Leaderboard topPlayers = new Leaderboard();
		String selectQuery = "SELECT * FROM user ORDER BY Total_Points DESC LIMIT 10";
		PreparedStatement statement = conn.prepareStatement(selectQuery);
		ResultSet rs = statement.executeQuery();
		// Storing top users into a leaderboard object to hold them.
		while (rs.next()) {
			User player = new User(rs.getString("Username"), rs.getInt("Total_Points"));
			topPlayers.insertUser(player);
		}
		return topPlayers;
	}

	// Returning the questions of a specific round for a specific user.
	QuestionHistory getQuestionHistory(int RoundID, int UserID) throws SQLException {
		QuestionHistory questions = new QuestionHistory(RoundID, UserID);
		String questionQuery = "SELECT * FROM `question` WHERE RoundID = ?";
		PreparedStatement statement = conn.prepareStatement(questionQuery);
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
	}

	// Returning the rounds for a specific user with their respective
	// QuestionHistory.
	RoundHistory getRoundHistory(int UserID) throws SQLException {
		RoundHistory rounds = new RoundHistory();
		int total_points = 0;
		String questionQuery = "SELECT * FROM `round` WHERE UserID = ?";
		PreparedStatement statement = conn.prepareStatement(questionQuery);
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
	}
}
