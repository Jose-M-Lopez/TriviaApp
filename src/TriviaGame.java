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

import javax.inject.Inject;

@Path("/api")
public class TriviaGame {
    //Connecting to database.
    @Inject
    TriviaGame(Database db) {
        triviaDB = db;
    }

    Database triviaDB;
    //Creating OkHttpClient to make API call.
    final OkHttpClient client = new OkHttpClient();
    //Creating JSON adapters in order to return and parse JSON.
    final Moshi moshi = new Moshi.Builder().build();
    final JsonAdapter<TriviaResponse> gistJsonAdapter = moshi.adapter(TriviaResponse.class);
    final JsonAdapter<QnA> QnAJsonAdapter = moshi.adapter(QnA.class);
    final JsonAdapter<CorrectCheck> CorrectCheckJsonAdapter = moshi.adapter(CorrectCheck.class);
    final JsonAdapter<Leaderboard> LeaderboardJsonAdapter = moshi.adapter(Leaderboard.class);
    final JsonAdapter<QuestionHistory> QuestionHistoryJsonAdapter = moshi.adapter(QuestionHistory.class);
    final JsonAdapter<RoundHistory> RoundHistoryJsonAdapter = moshi.adapter(RoundHistory.class);
    final JsonAdapter<GenericResponse> GenericResponseJsonAdapter = moshi.adapter(GenericResponse.class);

    //Path to return questions and answers.
    @GET
    @Path("/getQuestion")
    public String getQuestion(Session session) throws IOException {
        //Checking if user is logged in.
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not logged in.", true);
        }
        //Checking if a game is currently in progress.
        if (!session.isSet(SessionEnums.RoundID.name())) {
            return getGenericResponse("No round started.", true);
        }
        //Creating QnA object to send to client.
        QnA quest = new QnA();
        List<String> answers = new ArrayList<>();
        int index = session.get(SessionEnums.listIndex.name()).intValue();
        //Parsing response JSON and storing it into TriviaResponse object.
        TriviaResponse questions = gistJsonAdapter.fromJson(session.get(SessionEnums.questionList.name()).value());
        //Storing results from TriviaResponse object into QnA object.
        if ((questions.getResults().get(index).getQuestion() != null)) {
            //getResults is a list, need to use get(index) to access questions.
            quest.setQuestion(questions.getResults().get(index).getQuestion());
        }

        //Separating answers into either correct or incorrect inside of a string list.
        answers.addAll(questions.getResults().get(index).getIncorrect_answers());
        answers.add(questions.getResults().get(index).getCorrect_answer());
        //Shuffling location of correct answer for each question.
        Collections.shuffle(answers);
        //Inserting answers and question index into QnA object.
        quest.setAnswers(answers);
        quest.setCounter(index);
        //Setting question information for the session.
        session.set(SessionEnums.question.name(), quest.getQuestion());
        session.set(SessionEnums.category.name(), questions.getResults().get(index).getCategory());
        session.set(SessionEnums.correctAns.name(), questions.getResults().get(index).getCorrect_answer());
        //Returning encoded JSON.
        return QnAJsonAdapter.toJson(quest);
    }

    //Path to check whether or not user answered correctly.
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
        //Establishing correct answer before user submits response.
        answer.setcorrectAns(session.get(SessionEnums.correctAns.name()).value());
        int points = 0;
        //Checking if a score already exists, allows for points to carry on between questions, until the end of round.
        if (session.isSet(SessionEnums.score.name())) {
            points = session.get(SessionEnums.score.name()).intValue();
        }
        //Storing round difficulty and the questions correct answer.
        String difficulty = session.get(SessionEnums.difficulty.name()).value();
        String correctAnswer = session.get(SessionEnums.correctAns.name()).value();

        //If user answers correctly in 9 seconds or less, give them extra points.
        if (submittedAns.equals(correctAnswer)) {
            if (timer <= 9) {
                extraPoints = 50;
            }
            //Assigning different points based on question difficulty.
            answer.setifCorrect(true);
            switch (difficulty) {
                case "easy":
                    points += 100 + extraPoints;
                    break;
                case "medium":
                    points += 200 + extraPoints;
                    break;
                case "hard":
                    points += 300 + extraPoints;
                    break;
            }
            //Setting the user's answer as correct, and the amount of points earned.
            session.set(SessionEnums.score.name(), points);
            answer.setPoints(points);
            session.set(SessionEnums.ifCorrect.name(), true);

        } else {
            //Setting the user's answer as incorrect.
            session.set(SessionEnums.ifCorrect.name(), false);
            answer.setifCorrect(false);
        }
        //Inserting question and related values into the database.
        triviaDB.insertQuestion(session.get(SessionEnums.difficulty.name()).value(),
                session.get(SessionEnums.category.name()).value(), session.get(SessionEnums.question.name()).value(),
                session.get(SessionEnums.ifCorrect.name()).booleanValue(),
                session.get(SessionEnums.RoundID.name()).intValue());

        //Updating questionList index to obtain a new question.
        int index = session.get(SessionEnums.listIndex.name()).intValue();
        session.set(SessionEnums.listIndex.name(), ++index);

        //Cleaning up session between questions.
        session.unset(SessionEnums.question.name());
        session.unset(SessionEnums.correctAns.name());
        session.unset(SessionEnums.category.name());
        session.unset(SessionEnums.ifCorrect.name());
        return CorrectCheckJsonAdapter.toJson(answer);
    }

    //Allows user to begin a round.
    @POST
    @Path("/startGame")
    public String startGame(Session session, String difficulty) throws SQLException, IOException {
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not logged in.", true);
        }
        if (session.isSet(SessionEnums.RoundID.name())) {
            return getGenericResponse("Round already in progress.", true);
        }
        cleanSession(session);
        //Passes and sets user selected difficulty for the round.
        session.set(SessionEnums.difficulty.name(), difficulty);
        //Sets RoundID and inserts it into the database.
        session.set(SessionEnums.RoundID.name(), triviaDB.insertRound(session.get(SessionEnums.UserID.name()).intValue()));
        //Initializes score for the session.
        session.set(SessionEnums.score.name(), 0);

        //Requesting 10 questions from TriviaDB.
        String requestURL = "https://opentdb.com/api.php?amount=10" + "&difficulty="
                + session.get(SessionEnums.difficulty.name()).value() + "&type=multiple&encode=url3986";
        //Executing API call.
        Request request = new Request.Builder().url(requestURL).build();
        try (Response response = client.newCall(request).execute()) {
            //Checking whether or not the call was successful.
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            //Storing TriviaDB response in session variable.
            session.set(SessionEnums.questionList.name(), response.body().string());
            session.set(SessionEnums.listIndex.name(), 0);
        }
        return getGenericResponse("Round started");
    }

    //Ends round once user is done answering questions.
    @POST
    @Path("/endGame")
    public String endGame(Session session) throws SQLException {
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not logged in.", true);
        }
        if (!session.isSet(SessionEnums.RoundID.name())) {
            return getGenericResponse("No round started.", true);
        }
        //Inserts score user got for a specific round into database.
        triviaDB.updateRound(session.get(SessionEnums.score.name()).intValue(),
                session.get(SessionEnums.RoundID.name()).intValue());
        //Updates life-time points with amount earned in the round.
        triviaDB.updateTotalPoints(session.get(SessionEnums.score.name()).intValue(),
                session.get(SessionEnums.UserID.name()).intValue());

        QuestionHistory Results = triviaDB.getQuestionHistory(session.get(SessionEnums.RoundID.name()).intValue(),
                session.get(SessionEnums.score.name()).intValue());

        cleanSession(session);

        //Returning round results.
        return QuestionHistoryJsonAdapter.toJson(Results);
    }

    //Allows user to log in.
    @POST
    @Path("/Login")
    public String Login(Session session, String Username, String Password)
            throws SQLException, CannotPerformOperationException, InvalidHashException {

        //Checking if user is already logged in.
        if (session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Already logged in.", true);
        }
        //Passing user information from database into user object.
        User player = triviaDB.getUser(Username);
        //Checking if user exists.
        if (player == null) {
            return getGenericResponse("Incorrect password or username.", true);
        }

        session.set("Username", Username);

        //Verifying user entered password and username correctly.
        if (verifyPassword(Password, player.getHash())) {
            session.set(SessionEnums.UserID.name(), player.getID());

            //Cleaning up user's null questions and rounds from database.
            triviaDB.deleteIncompleteQuestions(session.get(SessionEnums.UserID.name()).intValue());
            triviaDB.deleteIncompleteRounds(session.get(SessionEnums.UserID.name()).intValue());

            return getGenericResponse("Successfully logged in.");
        } else {
            return getGenericResponse("Incorrect password or username.", true);
        }
    }

    //Allows user to log out.
    @POST
    @Path("/Logout")
    public String Logout(Session session) throws SQLException {
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not logged in.", true);
        }
        triviaDB.deleteIncompleteQuestions(session.get(SessionEnums.UserID.name()).intValue());
        triviaDB.deleteIncompleteRounds(session.get(SessionEnums.UserID.name()).intValue());

        //Terminating session.
        session.destroy();

        return getGenericResponse("You have been logged out.");
    }

    //Allows user to create account.
    @POST
    @Path("/createAccount")
    public String createAccount(Session session, String Username, String Password)
            throws SQLException, CannotPerformOperationException {
        //Various checks and restrictions for account creation.
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
        //Inserts new user information into the database.
        triviaDB.createUser(Username, createHash(Password));
        return getGenericResponse("Your account has been created.");
    }

    //Returns top ten players on the application.
    @GET
    @Path("/Leaderboard")
    public String Leaderboard() throws SQLException {
        return LeaderboardJsonAdapter.toJson(triviaDB.getTopTenPlayers());
    }

    //Returns questions for a specific round and the values associated with the questions.
    @GET
    @Path("/questionHistory")
    public String questionHistory(Session session, int RoundID) throws SQLException {
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not Logged In.", true);
        }
        return QuestionHistoryJsonAdapter.toJson(triviaDB.getQuestionHistory(RoundID, 0));
    }

    //Returns rounds associated with a specific user.
    @GET
    @Path("/roundHistory")
    public String roundHistory(Session session) throws SQLException {
        if (!session.isSet(SessionEnums.UserID.name())) {
            return getGenericResponse("Not Logged In.", true);
        }
        return RoundHistoryJsonAdapter
                .toJson(triviaDB.getRoundHistory(session.get(SessionEnums.UserID.name()).intValue()));
    }

    //Returns JSON strings.
    public String getGenericResponse(String response) {
        GenericResponse GR = new GenericResponse(response);
        return GenericResponseJsonAdapter.toJson(GR);
    }

    //Returns JSON strings and whether or not there's an error.
    public String getGenericResponse(String response, boolean error) {
        GenericResponse GR = new GenericResponse(response, error);
        return GenericResponseJsonAdapter.toJson(GR);
    }

    //Returns whether or not a player is logged in.
    @GET
    @Path("/checkLogin")
    public boolean checkLogin(Session session) {
        return session.isSet(SessionEnums.UserID.name());
    }

    //Returns whether or not a round is in progress.
    @GET
    @Path("/isRoundStarted")
    public boolean isRoundStart(Session session) {
        return session.isSet(SessionEnums.RoundID.name());
    }

    //Helper function to clean session between games.
    void cleanSession(Session session) {
        session.unset(SessionEnums.question.name());
        session.unset(SessionEnums.correctAns.name());
        session.unset(SessionEnums.category.name());
        session.unset(SessionEnums.difficulty.name());
        session.unset(SessionEnums.ifCorrect.name());
        session.unset(SessionEnums.RoundID.name());
        session.unset(SessionEnums.questionList.name());
        session.unset(SessionEnums.listIndex.name());
        session.unset(SessionEnums.score.name());
    }
}