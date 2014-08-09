package com.sicomeafit.campusmovil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class HttpHandler {
	
	/*
	 * Código de Apoyo: http://hmkcode.com/android-parsing-json-data/
	 * 					http://hmkcode.com/android-send-json-data-to-server/
	 */
	public ArrayList<JSONObject> getInformation(String postUrl, String wantedService, 
												HashMap<String, String> paramsForHttpPOST,
												Object httpRequest){

		String responseText = "";
		HttpClient httpClient = new DefaultHttpClient();
		JSONObject json = createJSONObject(wantedService, paramsForHttpPOST);
		HttpResponse httpResponse = null;
		InputStream inputStream = null;
		JSONArray responseJSONArray = null;
		ArrayList<JSONObject> responseJSON = new ArrayList<JSONObject>();
		
		//Se prepara la petición con los parámetros ya seteados.
		if(httpRequest instanceof HttpGet){
			//((HttpGet) httpRequest).setHeader("json", json.toString());
			((HttpGet) httpRequest).setHeader("Accept", "application/json");
			//((HttpGet) httpRequest).getParams().setParameter("jsonpost", json);
			((HttpGet) httpRequest).setHeader("Content-type", "application/json");
		}else if(httpRequest instanceof HttpPost){
			//((HttpPost) httpRequest).setHeader("json", json.toString());
			((HttpPost) httpRequest).setHeader("Accept", "application/json");
			//((HttpPost) httpRequest).getParams().setParameter("jsonpost", json);
			((HttpPost) httpRequest).setHeader("Content-type", "application/json");
			
			try{
				((HttpPost) httpRequest).setEntity(new StringEntity(json.toString()));
			}catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		//Se ejecuta la petición.
		try{
			if(httpRequest instanceof HttpGet){
				httpResponse = httpClient.execute((HttpGet) httpRequest);
			}else if(httpRequest instanceof HttpPost){
				httpResponse = httpClient.execute((HttpPost) httpRequest);
			}
			
		}catch(ClientProtocolException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try{
			inputStream = httpResponse.getEntity().getContent();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		 
        //Se convierte inputStream a String.
        if(inputStream != null){
            try{
				responseText = convertInputStreamToString(inputStream);
			}catch(IOException e){
				e.printStackTrace();
			}
        }else{
        	responseText = "Connection did not work!";
        }
        
		try{
			if(responseText.charAt(0) != '['){ //Breve conversión a JSONArray si es un sólo un JSONObject.
				responseText = "[" + responseText + "]"; 
			}
			
			responseJSONArray = new JSONArray(responseText);
			responseJSON = getJSONArrayOfJSONObjects(responseJSONArray);
		}catch (JSONException e){
			e.printStackTrace();
		}
        
		return responseJSON;
	}
	
	public JSONObject createJSONObject(String wantedService, HashMap<String, String> 
									   paramsForHttpPOST){
		JSONObject json = new JSONObject();
		
		try{
			//Servicio a ejecutar debido al parámetro wantedService.
			//json.put("service", wantedService);
			//Datos que se anexan al objeto tipo JSON.
			if(wantedService.equals("Login")){
				JSONObject internJSON = new JSONObject();
				internJSON.put("username", paramsForHttpPOST.get("username"));
				internJSON.put("password", paramsForHttpPOST.get("password"));
				json.put("user", internJSON);
			}else if(wantedService.equals("Register")){
				JSONObject internJSON = new JSONObject();
				internJSON.put("email", paramsForHttpPOST.get("email"));
				//json.put("regUsername", paramsForHttpPOST.get("regUsername"));
				internJSON.put("username", paramsForHttpPOST.get("regUsername"));
				//json.put("regPassword", paramsForHttpPOST.get("regPassword"));
				internJSON.put("password", paramsForHttpPOST.get("regPassword"));
				json.put("user", internJSON);
			}else if(wantedService.equals("Add user marker")){
				
			}else if(wantedService.equals("Make Suggestion")){
				/*json.put("email", paramsForHttpPOST.get("email"));
				json.put("username", paramsForHttpPOST.get("username"));
				json.put("suggestion", paramsForHttpPOST.get("suggestion"));
				*/
				JSONObject internJSON = new JSONObject();
				internJSON.put("message", paramsForHttpPOST.get("suggestion"));
				json.put("comment", internJSON);
			}
		}catch(JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }
        inputStream.close();
        
        return result;
    }
	
	private ArrayList<JSONObject> getJSONArrayOfJSONObjects(JSONArray responseJSONArray){
		ArrayList<JSONObject> responseJSON = new ArrayList<JSONObject>();
        for(int i = 0; i < responseJSONArray.length(); i++){
        	try {
				JSONObject JSONObject = new JSONObject(responseJSONArray.get(i).toString());
				responseJSON.add(JSONObject);
			}catch (JSONException e) {
				e.printStackTrace();
			}
        }
        
        return responseJSON;
    }
	
}
