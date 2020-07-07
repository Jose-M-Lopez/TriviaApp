package Models;

//Skeleton class to return strings as JSON.
public class GenericResponse {
    //Designates whether or not a message is an error.
    boolean error;
    private final String message;

    public GenericResponse(String message) {
        this.message = message;
    }

    public GenericResponse(String message, boolean error) {
        this.message = message;
        this.error = error;
    }
}