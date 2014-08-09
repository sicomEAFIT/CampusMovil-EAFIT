package com.sicomeafit.campusmovil;

public class UserData {
	
	private static String email;
	private static String username;
	private static String token;
	
	public UserData(String userEmail, String userUsername, String userToken){
		email = userEmail;
		username = userUsername;
		token = userToken;
	}

	public static String getEmail() {
		return email;
	}

	public static String getUsername() {
		return username;
	}

	public static String getToken() {
		return token;
	}	
	
}
