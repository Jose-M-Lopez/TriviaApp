import org.jooby.Jooby;

public class App extends Jooby {
	{
		// Launching application and defining URL paths.
		use(TriviaGame.class);
		assets("/", "index.html");
		assets("/**");
	}

	public static void main(final String[] args) {
		run(App::new, args);
	}
}