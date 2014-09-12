package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;


public class Suggestions extends Activity {

	private EditText etSuggestion;
	private String suggestion;
	
	//private String POST_URL = "http://www.profe5.com/sicom_trash/Android/makeSuggestion.php";
	private String POST_URL = "http://campusmovilapp.herokuapp.com/api/v1/comment?auth=" 
							  + UserData.getToken(); 
	private String wantedService;
	private HashMap<String, String> paramsForHttpPOST = new HashMap<String, String>();
	
	//Códigos HTTP.
	private static final int SUCCESS = 200;
	private static final int UNAUTHORIZED = 401;
	
	private static final int CLEAR_USER_DATA = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestions);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		initViewElements();
	}
	
	public void initViewElements(){
		etSuggestion = (EditText) findViewById(R.id.suggestion);
	}
	
	public void makeSuggestion(View v){
		suggestion = etSuggestion.getText().toString();
		if(suggestion.trim().isEmpty()){
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.suggestion_empty),
																				Toast.LENGTH_SHORT).show();
		}else{
			wantedService = "Make Suggestion";	
			paramsForHttpPOST.put("email", UserData.getEmail());
			paramsForHttpPOST.put("username", UserData.getUsername());
			paramsForHttpPOST.put("suggestion", suggestion);
			paramsForHttpPOST.put("suggestionUTF", convertToUTF8(suggestion));
			if(isInternetConnectionAvailable()){
				new POSTConnection().execute(POST_URL);
			}else{
				Toast.makeText(getApplicationContext(), getResources()
					           .getString(R.string.internet_connection_required), 
					           Toast.LENGTH_SHORT).show();
			}

		}
	}
	
	//Convierte String a codificación UTF-8.
	public static String convertToUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
    
	public boolean isInternetConnectionAvailable() {
		boolean connectionFound = false;
	    ConnectivityManager conMgr = (ConnectivityManager) 
	    							  getSystemService(Context.CONNECTIVITY_SERVICE);
	    if(conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable()
	       && conMgr.getActiveNetworkInfo().isConnected()) {
	        connectionFound = true;
	    }
	    return connectionFound;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){    //Ya se borró la info. del usuario y se puede proceder
	    	 							    //a una nueva autenticación.
		    	Intent returnToLogin = new Intent(Suggestions.this, MapAccess.class);
		    	Bundle statusInfo = new Bundle();
				statusInfo.putInt("status", UNAUTHORIZED);
				returnToLogin.putExtras(statusInfo);
				returnToLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
									   Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(returnToLogin);
				finish();         
			}
     
		 }
		
	} 
	
	
	/*
	 * Esta clase es necesaria para hacer la conexión con el Servidor.
	 * Hay que hacer una tarea asincrónica para que se pueda realizar correctamente
	 * el setContentView() en el UIThread.
	 */
	//parámetros, tipo de parámetro de método que indica progreso, tipo de parámetro de 
	                                                                  //método post- ejecución.
	 private class POSTConnection extends AsyncTask<String, Void, ArrayList<JSONObject>>{
		 
		private final ProgressDialog progressDialog = new ProgressDialog(Suggestions.this);
		private String url;
		private ArrayList<JSONObject> responseJSON = null;
		
		protected void onPreExecute() {
			this.progressDialog.setMessage(getString(R.string.sending_suggestion)); 
            this.progressDialog.show();
         }
		 
        protected ArrayList<JSONObject> doInBackground(String...urls) {
        	url = urls[0];
     		HttpHandler httpHandler = new HttpHandler();
     		responseJSON = httpHandler.getInformation(url, wantedService, paramsForHttpPOST, 
     												  new HttpPost(url));
     		
     		return responseJSON;
         }

         protected void onPostExecute(ArrayList<JSONObject> responseJSON) {  
        	 
        	 if(wantedService.equals("Make Suggestion")){
 				try {
 				    Log.i("responseJSON", responseJSON.toString());
 				    if(responseJSON.get(responseJSON.size()-1).getInt("status") == SUCCESS){
 				    	if(responseJSON.get(0).getBoolean("success")){ 
 				    		etSuggestion.setText("");
 							Toast.makeText(getApplicationContext(), getResources()
 										   .getString(R.string.suggestion_sent), Toast.LENGTH_LONG).show();
 							Intent openMap = new Intent(Suggestions.this, MapHandler.class); 
 							openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 							progressDialog.dismiss();
 							startActivity(openMap);
 				    	}else{
 				    		//Se muestra un diálogo para reintentar o cancelar el envío de la sugerencia.
 				    		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestions.this);
 							builder.setTitle(getResources().getString(R.string.oops));
 							builder.setMessage(getResources().getString(R.string.suggestion_not_sent));
 							
 							builder.setPositiveButton(getResources().getString(R.string.retry), 
 													  new OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									//Se intenta nuevamente la conexión con el servicio realizando 
 									//nuevamente la AsyncTask.
 									new POSTConnection().execute(POST_URL);
 								}
 							});
 							
 							builder.setNegativeButton(getResources().getString(R.string.cancel), 
									  new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									etSuggestion.setText("");
		 							Intent openMap = new Intent(Suggestions.this, MapHandler.class); 
		 							openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		 							startActivity(openMap);
								}
							});
 							
 							AlertDialog suggestionErrorDialog = builder.create();
 							progressDialog.dismiss();
 					    	suggestionErrorDialog.show();
 				    	}
					}else{
						if(responseJSON.get(responseJSON.size()-1).getInt("status") == UNAUTHORIZED){
				    		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestions.this);
							builder.setTitle(getResources().getString(R.string.log_in));
							builder.setMessage(getResources().getString(R.string.login_needed_2));
							
							builder.setPositiveButton(getResources().getString(R.string.log_in), 
													  new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//Se hace un startActivityForResult para que se borren
									//los datos del usuario. Luego se pasa al Log in.
									Intent clearUserData = new Intent(Suggestions.this, MapHandler.class);
									Bundle actionCode = new Bundle();
									actionCode.putInt("actionCode", CLEAR_USER_DATA);
									actionCode.putBoolean("isActivityForResult", true);
									clearUserData.putExtras(actionCode);
									startActivityForResult(clearUserData, 1);
									//El 1 indica que cuando la actividad finalice, retornara a
									//onActivityResult de esta actividad.
								}
							});
							
							builder.setNegativeButton(getResources().getString(R.string.exit), 
									  new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent exitApp = new Intent(Suggestions.this, MainActivity.class);
									Bundle userActionInfo = new Bundle();
									userActionInfo.putBoolean("exit", true);
									exitApp.putExtras(userActionInfo);
									exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
											   	 	 Intent.FLAG_ACTIVITY_CLEAR_TASK);
						    		startActivity(exitApp);
						    		finish();
								}
							});
							
							AlertDialog connectionErrorDialog = builder.create();
							progressDialog.dismiss();
					    	connectionErrorDialog.show();
						}else{
							/*
							Toast.makeText(getApplicationContext(), getResources()
								       	   .getString(R.string.connection_error),Toast.LENGTH_SHORT)
								       	   .show();
							*/
							AlertDialog.Builder builder = new AlertDialog.Builder(Suggestions.this);
							builder.setTitle(getResources().getString(R.string.connection_error_title));
							builder.setMessage(getResources().getString(R.string.connection_error));
							
							builder.setPositiveButton(getResources().getString(R.string.retry), 
													  new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new POSTConnection().execute(POST_URL);
								}
							});
							
							builder.setNegativeButton(getResources().getString(R.string.exit), 
									  new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent exitApp = new Intent(Suggestions.this, MainActivity.class);
									Bundle userActionInfo = new Bundle();
									userActionInfo.putBoolean("exit", true);
									exitApp.putExtras(userActionInfo);
									exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
											   	 	 Intent.FLAG_ACTIVITY_CLEAR_TASK);
						    		startActivity(exitApp);
						    		finish();
								}
							});
							
							AlertDialog connectionErrorDialog = builder.create();
							progressDialog.dismiss();
					    	connectionErrorDialog.show();
						}
			        }
 				} catch(JSONException e) {
					progressDialog.dismiss();
					e.printStackTrace();
				}
        	 }	
        	 
        	 paramsForHttpPOST.clear();
        	 
         }
	 }

	@Override
	 public boolean onOptionsItemSelected(MenuItem item){
		Intent openSelectedItem; 
	    switch (item.getItemId()){
		    case R.id.map:
	    		openSelectedItem = new Intent(Suggestions.this, MapHandler.class); 
	    		openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    		break;
	    	case R.id.places:
	    		openSelectedItem = new Intent(Suggestions.this, Places.class); 
	    		break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(Suggestions.this, AboutUs.class); 
	        	break;
	        case R.id.logout:
	        	Intent logOut = new Intent(Suggestions.this, MapHandler.class);
				Bundle actionCode = new Bundle();
				actionCode.putInt("actionCode", CLEAR_USER_DATA);
				logOut.putExtras(actionCode);
				logOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(logOut);
				finish();
	        	return true;
	        case android.R.id.home:
	        	finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	    startActivity(openSelectedItem);
	    return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
			menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
			menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
			menu.add(0, R.id.logout, Menu.FIRST+4, getResources().getString(R.string.log_out));
		}
		getMenuInflater().inflate(R.menu.suggestions, menu);
        
		return true;
	}

}
