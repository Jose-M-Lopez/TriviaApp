package Models;
//Skeleton class to return strings as JSON.
public class GenericResponse {
	private String message;
	boolean error; //designates whether a message is an error.

	public GenericResponse(String message) {
		this.message = message;
	}

	public GenericResponse(String message, boolean error) {
		this.message = message;
		this.error = error;
	}

}
