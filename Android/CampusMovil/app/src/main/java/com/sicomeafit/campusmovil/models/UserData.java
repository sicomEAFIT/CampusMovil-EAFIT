package com.sicomeafit.campusmovil.models;

import java.util.HashMap;
import java.util.Map;

public class UserData {

	private static String email;
	private static String username;
	private static String token;

    //Mapa que contiene la información de las notas guardadas por el usuario.
    public static Map<String, Note> userNotes = new HashMap<String, Note>();

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

	public static void setEmail(String email) {
		UserData.email = email;
	}

	public static void setUsername(String username) {
		UserData.username = username;
	}

	public static void setToken(String token) {
		UserData.token = token;
	}

    public static Map<String, Note> getUserNotes() {
        Map<String, Note> userNotesCopied = new HashMap<String, Note>();
        for(Map.Entry<String, Note> userNote : userNotes.entrySet()){
            userNotesCopied.put(userNote.getKey(), userNote.getValue());
        }
        return userNotesCopied;
    }

    public static void addUserNote(String title, Note note){
        userNotes.put(title, note);
    }

    public static void clearUserNotes(){
        userNotes.clear();
    }

}
