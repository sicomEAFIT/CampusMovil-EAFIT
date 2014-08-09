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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapAccess extends Activity {

	private RelativeLayout loginView;
	private EditText username;
	private EditText password;
	private String usernameString;
	private String passwordString;
	private RelativeLayout registerView;
	private EditText email;
	private EditText regUsername;
	private EditText regPassword;
	private EditText confPassword;
	private String emailString;
	private String confPasswordString;

	//private String POST_URL = "http://www.profe5.com/sicom_trash/Android/loginOrRegister.php";
	private String POST_URL = "";
	private String wantedService;
	private HashMap<String, String> paramsForHttpPOST = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_access);
		initViewElements();
	}

	public void initViewElements() {
		loginView = (RelativeLayout) findViewById(R.id.login_view);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		registerView = (RelativeLayout) findViewById(R.id.register_view);
		email = (EditText) findViewById(R.id.email);
		regUsername = (EditText) findViewById(R.id.regUsername);
		regPassword = (EditText) findViewById(R.id.regPassword);
		confPassword = (EditText) findViewById(R.id.confPassword);
		registerView.setVisibility(View.GONE);
		loginView.setVisibility(View.VISIBLE);
	}

	public void logIn(View v) {
		if (isAnyLoginFieldEmpty()) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.fill_in_all_fields),
					Toast.LENGTH_SHORT).show();
		} else {
			captureLoginInfo();
			wantedService = "Login";
			POST_URL = "http://campusmovilapp.herokuapp.com/api/v1/auth";
			paramsForHttpPOST.put("username", usernameString);
			paramsForHttpPOST.put("password", passwordString);
			if (isInternetConnectionAvailable()) {
				new POSTConnection().execute(POST_URL);
			} else {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.internet_connection_required),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public boolean isAnyLoginFieldEmpty() {
		if (username.getText().toString().trim().isEmpty()
				|| password.getText().toString().trim().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public void captureLoginInfo() {
		usernameString = username.getText().toString();
		passwordString = password.getText().toString();
	}

	public void goToRegisterView(View v) {
		loginView.setVisibility(View.GONE);
		registerView.setVisibility(View.VISIBLE);
	}

	public void register(View v) {
		if (isAnyRegisterFieldEmpty()) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.fill_in_all_fields),
					Toast.LENGTH_SHORT).show();
		} else {
			captureRegisterInfo();
			if (isEmailFormatCorrect()) {
				if (passwordString.equals(confPasswordString)) {
					POST_URL = "http://campusmovilapp.herokuapp.com/api/v1/register";
					wantedService = "Register";
					paramsForHttpPOST.put("email", emailString);
					paramsForHttpPOST.put("regUsername", usernameString);
					paramsForHttpPOST.put("regPassword", passwordString);
					if (isInternetConnectionAvailable()) {
						new POSTConnection().execute(POST_URL);
					} else {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.internet_connection_required),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.pass_and_confpass_not_equal),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.email_format_not_valid),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public boolean isAnyRegisterFieldEmpty() {
		if (email.getText().toString().trim().isEmpty()
				|| regUsername.getText().toString().trim().isEmpty()
				|| regPassword.getText().toString().trim().isEmpty()
				|| confPassword.getText().toString().trim().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public void captureRegisterInfo() {
		emailString = email.getText().toString();
		usernameString = regUsername.getText().toString();
		passwordString = regPassword.getText().toString();
		confPasswordString = confPassword.getText().toString();
	}

	public boolean isEmailFormatCorrect() {
		if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
			return true;
		} else {
			return false;
		}
	}

	public void returnToLoginView(View v) {
		email.setText("");
		regUsername.setText("");
		regPassword.setText("");
		confPassword.setText("");
		registerView.setVisibility(View.GONE);
		loginView.setVisibility(View.VISIBLE);
	}

	public boolean isInternetConnectionAvailable() {
		boolean connectionFound = false;
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnected()) {
			connectionFound = true;
		}
		return connectionFound;
	}

	/*
	 * Esta clase es necesaria para hacer la conexión con el Servidor. Hay que
	 * hacer una tarea asincrónica para que se pueda realizar correctamente el
	 * setContentView() en el UIThread.
	 */
	// parámetros, tipo de parámetro de método que indica progreso, tipo de
	// parámetro de
	// método post- ejecución.
	private class POSTConnection extends
			AsyncTask<String, Void, ArrayList<JSONObject>> {

		private final ProgressDialog progressDialog = new ProgressDialog(
				MapAccess.this);
		private String url;
		private ArrayList<JSONObject> responseJSON = null;

		protected void onPreExecute() {
			if (wantedService.equals("Login")) {
				this.progressDialog.setMessage(getString(R.string.logging_in));
			} else if (wantedService.equals("Register")) {
				this.progressDialog.setMessage(getString(R.string.registering));
			}
			this.progressDialog.show();
		}

		protected ArrayList<JSONObject> doInBackground(String... urls) {
			url = urls[0];
			HttpHandler httpHandler = new HttpHandler();
			responseJSON = httpHandler.getInformation(url, wantedService,paramsForHttpPOST, 
													  new HttpPost(url));

			return responseJSON;
		}

		protected void onPostExecute(ArrayList<JSONObject> responseJSON) {        	 
        	if(wantedService.equals("Login")){
				try{
					Log.i("responseJSON", responseJSON.toString());
					if(responseJSON.size() != 0){
						if(responseJSON.get(0).getBoolean("success") &&
						   responseJSON.get(0).getJSONObject("auth") != null){ 
							 //Se comprueba que haya sido exitosa la conexión con el Servidor. 
							
							//Se almacenan los datos del usuario para reconocerlo después.
							String serviceEmail = responseJSON.get(0).getJSONObject("auth")
												  .getJSONObject("user").getString("email");
							String serviceUsername = responseJSON.get(0).getJSONObject("auth")
									  				 .getJSONObject("user").getString("username");
							String authoToken = responseJSON.get(0).getJSONObject("auth")
											   .getString("token");
							new UserData(serviceEmail, serviceUsername, authoToken);
							Intent openCampusMap = new Intent(MapAccess.this, MapHandler.class);
							Bundle userInfo = new Bundle();
							userInfo.putString("username", serviceUsername);
							openCampusMap.putExtras(userInfo);
							progressDialog.dismiss();
							startActivity(openCampusMap);
							finish();
						}else{
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), getResources()
									       .getString(R.string.login_failure),Toast.LENGTH_SHORT)
									       .show();
						}
					}else{
						progressDialog.dismiss();
						/*
						Toast.makeText(getApplicationContext(), getResources()
							       	   .getString(R.string.connection_error),Toast.LENGTH_SHORT)
							       	   .show();
						*/
						AlertDialog.Builder builder = new AlertDialog.Builder(MapAccess.this);
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
				}catch(JSONException e) {
					progressDialog.dismiss();
					e.printStackTrace();
				}
        	}else if(wantedService.equals("Register")){
				try{
					Log.i("responseJSON", responseJSON.toString());
					if(responseJSON.size() != 0){
						if(responseJSON.get(0).getBoolean("success")){ 
							//Se comprueba que haya sido exitosa la conexión con el Servidor.

							returnToLoginView(null);  //No hay necesidad de enviar ningún 
							 						 //parámetro para que se ejecute la acción 
													//deseada.
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), getResources()
														  .getString(R.string.register_success),
																	  Toast.LENGTH_LONG).show();
							}else{
								progressDialog.dismiss();
								Toast.makeText(getApplicationContext(), getResources()
									  .getString(R.string.register_user_already_exists_failure),
											Toast.LENGTH_LONG).show();
							}
					}else{
						progressDialog.dismiss();
						Toast.makeText(getApplicationContext(), getResources()
						       	   			.getString(R.string.connection_error),
						       	   			Toast.LENGTH_SHORT).show();
					}
				}catch(JSONException e) {
					progressDialog.dismiss();
					e.printStackTrace();
				}
        	}
        	paramsForHttpPOST.clear();
         }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent openSelectedItem;
		switch (item.getItemId()) {
		case R.id.aboutUs:
			openSelectedItem = new Intent(MapAccess.this, AboutUs.class);
			Bundle originInfo = new Bundle();
			originInfo.putString("originActivity", "Access"); // Se hace para
																// indicar que
																// procedemos de
																// la actividad
																// inicial con
																// el fin de
																// saber
																// qué mostrar
																// en el menú.
			openSelectedItem.putExtras(originInfo);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		startActivity(openSelectedItem);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (ViewConfiguration.get(getApplicationContext())
				.hasPermanentMenuKey()) {
			menu.add(0, R.id.aboutUs, Menu.FIRST + 1,
					getResources().getString(R.string.about_us));
		}
		getMenuInflater().inflate(R.menu.map_access, menu);
		return true;
	}

}
