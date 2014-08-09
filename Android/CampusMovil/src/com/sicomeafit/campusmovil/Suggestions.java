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
			if(isInternetConnectionAvailable()){
				new POSTConnection().execute(POST_URL);
			}else{
				Toast.makeText(getApplicationContext(), getResources()
					           .getString(R.string.internet_connection_required), 
					           Toast.LENGTH_SHORT).show();
			}

		}
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
	
	
	/*
	 * Esta clase es necesaria para hacer la conexi�n con el Servidor.
	 * Hay que hacer una tarea asincr�nica para que se pueda realizar correctamente
	 * el setContentView() en el UIThread.
	 */
	//par�metros, tipo de par�metro de m�todo que indica progreso, tipo de par�metro de 
	                                                                  //m�todo post- ejecuci�n.
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
 				    if(responseJSON.size() != 0){
 				    	if(responseJSON.get(0).getBoolean("success")){ 
 				    		etSuggestion.setText("");
 							Toast.makeText(getApplicationContext(), getResources()
 										   .getString(R.string.suggestion_sent), Toast.LENGTH_LONG).show();
 							Intent openMap = new Intent(Suggestions.this, MapHandler.class); 
 							openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 							progressDialog.dismiss();
 							startActivity(openMap);
 				    	}else{
 				    		//Se muestra un di�logo para reintentar o cancelar el env�o de la sugerencia.
 				    		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestions.this);
 							builder.setTitle(getResources().getString(R.string.oops));
 							builder.setMessage(getResources().getString(R.string.suggestion_not_sent));
 							
 							builder.setPositiveButton(getResources().getString(R.string.retry), 
 													  new OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									//Se intenta nuevamente la conexi�n con el servicio realizando 
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
 							
 							progressDialog.dismiss();
 							AlertDialog suggestionErrorDialog = builder.create();
 					    	suggestionErrorDialog.show();
 				    	}
					}else{
						progressDialog.dismiss();
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
								System.exit(0);
							}
						});
						
						AlertDialog connectionErrorDialog = builder.create();
				    	connectionErrorDialog.show();
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
		}
		getMenuInflater().inflate(R.menu.suggestions, menu);
		return true;
	}

}