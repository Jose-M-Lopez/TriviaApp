import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;

public class App extends Jooby {
    {
        //Launching application, defining URL paths, and initializing database.
        use(TriviaGame.class);
        use(new Jdbc("db"));
        assets("/", "index.html");
        assets("/**");
        assets("/favicon.ico");
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }
}