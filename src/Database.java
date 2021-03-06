import Models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import javax.sql.DataSource;

public class Database {
    //Initializing data source and storing database instance.
    DataSource datasource;

    @Inject
    public Database(DataSource ds) {
        datasource = ds;
    }

    //Passing query into database to insert questions.
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

        } finally {
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to insert rounds.
    int insertRound(int UserID) throws SQLException {
        PreparedStatement statement = null;
        Connection conn = null;
        String insertionQuery = "INSERT INTO round (UserID)" + " VALUES (?);";

        try {
            conn = datasource.getConnection();
            //Parameter Statement.RETURN_GENERATED_KEYS allows for generated key values such as RoundID to be returned.
            statement = conn.prepareStatement(insertionQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, UserID);
            statement.executeUpdate();
            //Storing returned generated keys in Result.
            ResultSet Result = statement.getGeneratedKeys();
            Result.next();
            return Result.getInt(1);

        } finally {
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to update a round's points once it has ended.
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
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to create new user.
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
            closeConnection(statement, conn);
        }
    }

    //Returning User object that contains user information from Database.
    User getUser(String Username) throws SQLException {
        PreparedStatement statement = null;
        PreparedStatement checkStatement;
        Connection conn = null;
        String selectCount = "SELECT COUNT(UserID) FROM user WHERE Username = ?";

        try {
            conn = datasource.getConnection();
            checkStatement = conn.prepareStatement(selectCount);
            checkStatement.setString(1, Username);
            ResultSet checkRS = checkStatement.executeQuery();
            checkRS.next();
            //Returning null if user does not exist within the database.
            if (checkRS.getInt(1) == 0) {
                return null;
            }
            //Storing user information into getUser object if they exist.
            String selectQuery = "SELECT * FROM user WHERE Username = ?";
            statement = conn.prepareStatement(selectQuery);
            statement.setString(1, Username);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return new User(rs.getString("Username"), rs.getString("Password"), rs.getInt("UserID"));

        } finally {
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to update life-time points for a user once a round has ended.
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
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to remove rounds with null point values(incomplete rounds).
    void deleteIncompleteRounds(int userID) throws SQLException {
        PreparedStatement statement = null;
        Connection conn = null;
        String deleteQuery = "DELETE FROM round WHERE Points_Earned IS NULL " +
                "AND UserID = ?";
        try {
            conn = datasource.getConnection();
            statement = conn.prepareStatement(deleteQuery);
            statement.setInt(1, userID);
            statement.executeUpdate();
        } finally {
            closeConnection(statement, conn);
        }
    }

    //Passing query into database to remove questions associated with an incomplete round.
    void deleteIncompleteQuestions(int userID) throws SQLException {
        PreparedStatement statement = null;
        Connection conn = null;
        String deleteQuery = "DELETE FROM `question` WHERE RoundID IN"
                + "(SELECT RoundID FROM round WHERE Points_earned IS NULL AND UserID = ?)";
        try {
            conn = datasource.getConnection();
            statement = conn.prepareStatement(deleteQuery);
            statement.setInt(1, userID);
            statement.executeUpdate();
        } finally {
            closeConnection(statement, conn);
        }
    }

    //Returning the ten users with the greatest life-time points.
    Leaderboard getTopTenPlayers() throws SQLException {
        PreparedStatement statement = null;
        Connection conn = null;
        Leaderboard topPlayers = new Leaderboard();
        String selectQuery = "SELECT * FROM user ORDER BY Total_Points DESC LIMIT 10";
        try {
            conn = datasource.getConnection();
            statement = conn.prepareStatement(selectQuery);
            ResultSet rs = statement.executeQuery();
            //Storing top users in a leaderboard object.
            while (rs.next()) {
                User player = new User(rs.getString("Username"), rs.getInt("Total_Points"));
                topPlayers.insertUser(player);
            }
            return topPlayers;
        } finally {
            closeConnection(statement, conn);
        }
    }

    //Returning the questions of a specific round for a specific user.
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
            //While there are still more questions.
            while (rs.next()) {
                //Inserting question information into question object,
                //which then gets inserted into a QuestionHistory object.
                Question question = new Question(rs.getString("Question"), rs.getString("Difficulty"),
                        rs.getString("Category"), rs.getBoolean("ifCorrect"));
                questions.insertQuestion(question);
            }
            return questions;
        } finally {
            closeConnection(statement, conn);
        }
    }

    //Returning the rounds for a specific user with their respective QuestionHistory.
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
                //Inserting QuestionHistories into a RoundHistory object.
                total_points += rs.getInt("Points_Earned");
                QuestionHistory questions = new QuestionHistory(rs.getInt("RoundID"),
                        rs.getInt("Points_Earned"));
                rounds.insertQuestionHistory(questions);
            }
            rounds.setPoints(total_points);
            return rounds;
        } finally {
            closeConnection(statement, conn);
        }
    }

    //Helper function to check and close statement/connection.
    void closeConnection(PreparedStatement statement, Connection conn) throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}