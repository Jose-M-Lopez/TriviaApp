import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;

public class App extends Jooby {
	{
		
		// Launching application and defining URL paths.
		use(TriviaGame.class);
		use(new Jdbc("db"));
		assets("/", "index.html");
		assets("/**");
	}

	public static void main(final String[] args) {
		run(App::new, args);
	}
}