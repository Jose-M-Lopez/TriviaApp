import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;

public class App extends Jooby {
    {
        //Launching application, defining URL paths, and initializing database.
        //Path: What browser is asking for, Location: File path in directory
        use(TriviaGame.class);
        use(new Jdbc("db"));
        assets("/", "index.html");
        assets("/favicon.ico","images/favicon.ico");
        assets("/**");
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }
}