import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import org.jooby.Session;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import Models.*;
import static Utility.PasswordStorage.*;
import okhttp3.*;
import java.util.Random;

import javax.inject.Inject;

@Path("/api")
public class TriviaGame {

	@Inject
	TriviaGame(Database db) throws SQLException 
	{
		triviaDB = db; // Initializing database.
	}

	Database triviaDB;
	// Creating OkHttpClient to make API call.
	final OkHttpClient client = new OkHttpClient();
	// Creating JSON adapters in order to return/parse JSON.
	final Moshi moshi = new Moshi.Builder().build();
	final JsonAdapter<TriviaResponse> gistJsonAdapter = moshi.adapter(TriviaResponse.class);
	final JsonAdapter<QnA> QnAJsonAdapter = moshi.adapter(QnA.class);
	final JsonAdapter<CorrectCheck> CorrectCheckJsonAdapter = moshi.adapter(CorrectCheck.class);
	final JsonAdapter<Leaderboard> LeaderboardJsonAdapter = moshi.adapter(Leaderboard.class);
	final JsonAdapter<QuestionHistory> QuestionHistoryJsonAdapter = moshi.adapter(QuestionHistory.class);
	final JsonAdapter<RoundHistory> RoundHistoryJsonAdapter = moshi.adapter(RoundHistory.class);
	final JsonAdapter<GenericResponse> GenericResponseJsonAdapter = moshi.adapter(GenericResponse.class);
	final JsonAdapter<Token> TokenJsonAdapter = moshi.adapter(Token.class);

	// Array of potential categories for the questions.
	int[] Categories = { 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 };

	/*							---CATEGORIES--- 
	 * 9:General Knowledge, 10:Books, 11:Film, 12:Music, 13:Musicals
	 * & Theater, 14:Television, 15:Video Games, 16:Board Games, 17:Science & Nature,
	 * 18:Computers, 19:Mathematics, 20:Mythology, 21:Sports, 22:Geography, 
	 * 23:History, 24:Politics, 25:Art, 26:Celebrities, 27:Animals, 28:Vehicles, 
	 * 29: Comics, 30:Gadgets, 31:Japanese Anime & Manga, 32:Cartoons & Animations
	 */

	Random rand = new Random();

	// Path to return questions and answers.
	@Path("/getQuestion")
	@GET // POST?
	public String getQuestion(Session session) throws IOException {
		// Checking if user is logged in.
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not logged in.", true);
		}
		// Checking if a game is currently in progress.
		if (!session.isSet(SessionEnums.RoundID.name())) {
			return getGenericResponse("No round started.", true);
		}
		QnA Quest = new QnA();
		// Randomizing round selection.
		int randomCategory = Categories[rand.nextInt(Categories.length)];
		List<String> answers = new ArrayList<String>();
		// Creating API call to receive questions.
		String requestURL = "https://opentdb.com/api.php?amount=1&category=" + randomCategory + "&difficulty="
				+ session.get(SessionEnums.difficulty.name()).value() + "&type=multiple&encode=url3986&token="
				+ session.get(SessionEnums.token.name()).value();
		// Executing API call.
		Request request = new Request.Builder().url(requestURL).build();
		try (Response response = client.newCall(request).execute()) {
			// Checking whether or not the call was successful.
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);
			// Parsing response JSON and storing it into TriviaResponse object.
			TriviaResponse questions = gistJsonAdapter.fromJson(response.body().source());
			// Storing results from TriviaResponse object into QnA object.
			if ((questions.getResults().get(0).getQuestion() != null)) {
				Quest.setQuestion(questions.getResults().get(0).getQuestion()); // getResults is a list, need to use
																				// .get(element) to access questions.

			}

			// Separating answers into either correct or incorrect inside of a string list.
			answers.addAll(questions.getResults().get(0).getIncorrect_answers());
			answers.add(questions.getResults().get(0).getCorrect_answer());
			// Shuffling location of correct answer each time.
			Collections.shuffle(answers);
			// Inserting answers into QnA object.
			Quest.setAnswers(answers);
			// Setting the question information for the session.
			session.set(SessionEnums.question.name(), Quest.getQuestion());
			session.set(SessionEnums.category.name(), questions.getResults().get(0).getCategory());
			session.set(SessionEnums.correctAns.name(), questions.getResults().get(0).getCorrect_answer());
			// Returning encoded JSON.
			return QnAJsonAdapter.toJson(Quest);
		}
	}

	// Path to check whether or not user answered correctly.
	@POST
	@Path("/Check")
	public String Check(Session session, String submittedAns, int timer) throws SQLException {
		int extraPoints = 0;
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not logged in.", true);
		}
		if (!session.isSet(SessionEnums.RoundID.name())) {
			return getGenericResponse("No round started.", true);
		}
		CorrectCheck answer = new CorrectCheck();
		// Establishing correct answer before user submits response.
		answer.setcorrectAns(session.get(SessionEnums.correctAns.name()).value());
		int points = 0;
		// Checking if a score already exists, allows for points to carry on between
		// questions,
		// until end of round.
		if (session.isSet(SessionEnums.score.name())) {
			points = session.get(SessionEnums.score.name()).intValue();
		}
		// Storing round difficulty and the questions correct answer.
		String difficulty = session.get(SessionEnums.difficulty.name()).value();
		String correctAnswer = session.get(SessionEnums.correctAns.name()).value();

		// If user answers correctly in 9 seconds or less, give them extra points.
		if (submittedAns.equals(correctAnswer)) {
			if (timer <= 9) {
				extraPoints = 50;
			}
			// Assigning different points based on question difficulty.
			answer.setifCorrect(true);
			if (difficulty.equals("easy")) {
				points += 100 + extraPoints;
			} else if (difficulty.equals("medium")) {
				points += 200 + extraPoints;
			} else if (difficulty.equals("hard")) {
				points += 300 + extraPoints;
			}
			// Setting that the user got the question correct, and the amount of points
			// earned.
			session.set(SessionEnums.score.name(), points);
			answer.setPoints(points);
			session.set(SessionEnums.ifCorrect.name(), true);

		} else {
			// Setting that the user got the question incorrect.
			session.set(SessionEnums.ifCorrect.name(), false);
			answer.setifCorrect(false);
		}
		// Inserting question and related values into the database.
		triviaDB.insertQuestion(session.get(SessionEnums.difficulty.name()).value(),
				session.get(SessionEnums.category.name()).value(), session.get(SessionEnums.question.name()).value(),
				session.get(SessionEnums.ifCorrect.name()).booleanValue(),
				session.get(SessionEnums.RoundID.name()).intValue());

		// Cleaning up session in-between questions.
		session.unset(SessionEnums.question.name());
		session.unset(SessionEnums.correctAns.name());
		session.unset(SessionEnums.category.name());
		session.unset(SessionEnums.ifCorrect.name());
		return CorrectCheckJsonAdapter.toJson(answer);
	}

	// Allows user to begin a round.
	@POST
	@Path("/startGame")
	public String startGame(Session session, String difficulty) throws SQLException, IOException {
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not logged in.", true);
		}
		if (session.isSet(SessionEnums.RoundID.name())) {
			return getGenericResponse("Round already in progress.", true);
		}

		// Creating a token that stops questions from repeating.
		String requestToken = "https://opentdb.com/api_token.php?command=request";
		Request request = new Request.Builder().url(requestToken).build();
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);

			Token token = TokenJsonAdapter.fromJson(response.body().source());

			// Cleaning up session in-between rounds.
			session.unset(SessionEnums.question.name());
			session.unset(SessionEnums.correctAns.name());
			session.unset(SessionEnums.category.name());
			session.unset(SessionEnums.difficulty.name());
			session.unset(SessionEnums.RoundID.name());
			session.unset(SessionEnums.ifCorrect.name());
			session.unset(SessionEnums.score.name());
			session.unset(SessionEnums.token.name());

			// Passes and sets user selected difficulty for the round.
			String diff = difficulty;
			session.set(SessionEnums.difficulty.name(), diff);
			// Sets RoundID and inserts it into the database.
			session.set(SessionEnums.RoundID.name(),
					triviaDB.insertRound(session.get(SessionEnums.UserID.name()).intValue()));
			// Sets token for the session.
			session.set(SessionEnums.token.name(), token.getToken());
			session.set(SessionEnums.score.name(), 0);

			return getGenericResponse("Round started");
		}
	}

	// Finishes round once user is done answering questions.
	@POST
	@Path("/endGame")
	public String endGame(Session session) throws SQLException {
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not logged in.", true);
		}
		if (!session.isSet(SessionEnums.RoundID.name())) {
			return getGenericResponse("No round started.", true);
		}
		// Inserts score user got for a specific round into database.
		triviaDB.updateRound(session.get(SessionEnums.score.name()).intValue(),
				session.get(SessionEnums.RoundID.name()).intValue());
		// Updates total points with amount earned in the round.
		triviaDB.updateTotalPoints(session.get(SessionEnums.score.name()).intValue(),
				session.get(SessionEnums.UserID.name()).intValue());

		QuestionHistory Results = triviaDB.getQuestionHistory(session.get(SessionEnums.RoundID.name()).intValue(),
				session.get(SessionEnums.score.name()).intValue());
		session.unset(SessionEnums.question.name());
		session.unset(SessionEnums.correctAns.name());
		session.unset(SessionEnums.category.name());
		session.unset(SessionEnums.difficulty.name());
		session.unset(SessionEnums.ifCorrect.name());
		session.unset(SessionEnums.token.name());
		session.unset(SessionEnums.RoundID.name());

		// returning round results.
		return QuestionHistoryJsonAdapter.toJson(Results);
	}

	// Allow user to log in.
	@POST
	@Path("/Login")
	public String Login(Session session, String Username, String Password)
			throws SQLException, CannotPerformOperationException, InvalidHashException {

		// Checking if user is already logged in.
		if (session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Already logged in.", true);
		}
		// Cleaning up null questions and rounds from database.
		triviaDB.deleteIncompleteQuestions();
		triviaDB.deleteIncompleteRounds();
		// Checking if user exists.
		if (triviaDB.getUser(Username) == null) {
			return getGenericResponse("Incorrect password or username.", true);
		}
		// Passing player information from user database into user object.
		User player = triviaDB.getUser(Username);
		session.set("Username", Username);

		// Verifying user entered password and username correctly.
		if (verifyPassword(Password, player.getHash())) {
			session.set(SessionEnums.UserID.name(), player.getID());
			return getGenericResponse("Successfully logged in.");
		} else {
			return getGenericResponse("Incorrect password or username.", true);
		}
	}

	// Allow user to log out.
	@POST
	@Path("/Logout")
	public String Logout(Session session) throws SQLException {
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not logged in.", true);
		}
		triviaDB.deleteIncompleteQuestions();
		triviaDB.deleteIncompleteRounds();
		// terminating session.
		session.destroy();
		return getGenericResponse("You have been logged out.");
	}

	// Allow users to create accounts.
	@POST
	@Path("/createAccount")
	public String createAccount(Session session, String Username, String Password)
			throws SQLException, CannotPerformOperationException {
		if (Username.length() == 0) {
			return getGenericResponse("Username cannot be empty.");
		}
		if (Username.length() > 20) {
			return getGenericResponse("Username cannot be more than 20 characters.");
		}
		if (Username.length() - Username.replaceAll(" ", "").length() > 0) {
			return getGenericResponse("Username cannot contain spaces.");
		}
		if (session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Already logged in.", true);
		}
		if (triviaDB.getUser(Username) != null) {
			return getGenericResponse("Account already exists.", true);
		}
		if (Password.length() < 6) {
			return getGenericResponse("Password is too short.", true);
		}
		if (Password.length() - Password.replaceAll(" ", "").length() > 0) {
			return getGenericResponse("Password cannot contain spaces.");
		}
		// Inserts new user information into the database.
		triviaDB.createUser(Username, createHash(Password));
		return getGenericResponse("Your account has been created.");
	}

	// Returns top ten players on the application.
	@GET
	@Path("/Leaderboard")
	public String Leaderboard() throws SQLException {
		return LeaderboardJsonAdapter.toJson(triviaDB.getTopTenPlayers());
	}

	// Returns questions for a specific round and the values associated with the
	// questions.
	@GET
	@Path("/questionHistory")
	public String questionHistory(Session session, int RoundID) throws SQLException {
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not Logged In.", true);
		}
		return QuestionHistoryJsonAdapter.toJson(triviaDB.getQuestionHistory(RoundID, 0));
	}

	// Returns rounds associated with a specific user.
	@GET
	@Path("/roundHistory")
	public String roundHistory(Session session) throws SQLException {
		if (!session.isSet(SessionEnums.UserID.name())) {
			return getGenericResponse("Not Logged In.", true);
		}
		return RoundHistoryJsonAdapter
				.toJson(triviaDB.getRoundHistory(session.get(SessionEnums.UserID.name()).intValue()));
	}

	// Returns JSON strings.
	public String getGenericResponse(String response) {
		GenericResponse GR = new GenericResponse(response);
		return GenericResponseJsonAdapter.toJson(GR);
	}

	// Returns JSON strings and whether or not it's an error.
	public String getGenericResponse(String response, boolean error) {
		GenericResponse GR = new GenericResponse(response, error);
		return GenericResponseJsonAdapter.toJson(GR);
	}

	// Returns whether a player is logged in or not
	@GET
	@Path("/checkLogin")
	public boolean checkLogin(Session session) throws SQLException {
		if (session.isSet(SessionEnums.UserID.name())) {
			return true;
		}
		return false;
	}

	// Returns whether a player is logged in or not
	@GET
	@Path("/isRoundStarted")
	public boolean isRoundStart(Session session) throws SQLException {
		if (session.isSet(SessionEnums.RoundID.name())) {
			return true;
		}
		return false;
	}
}