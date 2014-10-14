package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import org.json.JSONObject;

public interface SubscribedActivities {

	public void notify(String action, ArrayList<JSONObject> responseJson);
	
}
