package Models;
//Skeleton class to hold session token in order to prevent questions from repeating.
public class Token 
{
	String token;
	
	public Token(String token)
	{
		this.token = token;
	}
	
	public String getToken()
	{
		return token;
	}

}
