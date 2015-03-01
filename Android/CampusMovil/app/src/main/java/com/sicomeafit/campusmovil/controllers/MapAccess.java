package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.helpers.HttpHandler;
import com.sicomeafit.campusmovil.models.UserData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MapAccess extends Activity implements SubscribedActivities{

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

	private HttpHandler httpHandler = new HttpHandler();
	private final String ACTION_AUTH = "/auth";
	private final String ACTION_REGISTER = "/register";
	private Map<String, String> paramsForHttpPost = new HashMap<String, String>();

	//Navigation Drawer (men� lateral).
	ListView drawer = null;
	DrawerLayout drawerLayout = null;
	ActionBarDrawerToggle toggle = null;
	ArrayList<String> menuToShow = new ArrayList<String>(); 
	ArrayList<Integer> menuToShowIds = new ArrayList<Integer>(); 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_access);
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		initViewElements();
		//Se indica al HttpHandler la actividad que estar� esperando la respuesta a la petici�n.
		httpHandler.addListeningActivity(this);
	}

	public void setNavigationDrawer(){
		drawer = (ListView) findViewById(R.id.drawer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				drawerLayout.closeDrawers();
				handleMenuEvents(menuToShowIds.get(arg2));
			}
		});

		toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name ){

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}
		};

		drawerLayout.setDrawerListener(toggle);
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
			paramsForHttpPost.put("username", usernameString);
			paramsForHttpPost.put("password", passwordString);
			if (httpHandler.isInternetConnectionAvailable(this)) {
				httpHandler.sendRequest(HttpHandler.API_V1, ACTION_AUTH, "", paramsForHttpPost, 
						new HttpPost(), MapAccess.this);
			} else {
				Toast.makeText(getApplicationContext(),getResources()
						.getString(R.string.internet_connection_required),Toast.LENGTH_SHORT)
						.show();
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
        email.requestFocus();
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
					paramsForHttpPost.put("email", emailString);
					paramsForHttpPost.put("regUsername", usernameString);
					paramsForHttpPost.put("regPassword", passwordString);
					if (httpHandler.isInternetConnectionAvailable(this)) {
						httpHandler.sendRequest(HttpHandler.API_V1, ACTION_REGISTER, "", 
								paramsForHttpPost, new HttpPost(), MapAccess.this);
					} else {
						Toast.makeText(getApplicationContext(),getResources()
								.getString(R.string.internet_connection_required),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),getResources()
							.getString(R.string.pass_and_confpass_not_equal),Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				Toast.makeText(getApplicationContext(),getResources()
						.getString(R.string.email_format_not_valid),Toast.LENGTH_LONG).show();
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

	@Override  //Se utiliza para sincronizar el estado del Navigation Drawer (men� lateral).
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.map:
			openSelectedItem = new Intent(MapAccess.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(MapAccess.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(MapAccess.this, AboutUs.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		}
		startActivity(openSelectedItem);
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (toggle.onOptionsItemSelected(item)) {
			return true; //Hace que se abra el men� lateral al presionar el �cono.
		}
		Intent openSelectedItem;
		switch (item.getItemId()) {
		case R.id.map:
			openSelectedItem = new Intent(MapAccess.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.places:
			openSelectedItem = new Intent(MapAccess.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.aboutUs:
			openSelectedItem = new Intent(MapAccess.this, AboutUs.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		startActivity(openSelectedItem);
		return true;
	}

	@Override
	public void notify(String action, ArrayList<JSONObject> responseJson) {
		if(action.equals(ACTION_AUTH)){
			try{
				Log.i("responseJson", responseJson.toString());
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
					//Se almacenan los datos del usuario para reconocerlo despu�s.
					String serviceEmail = responseJson.get(0).getJSONObject("auth")
							.getJSONObject("user").getString("email");
					String serviceUsername = responseJson.get(0).getJSONObject("auth")
							.getJSONObject("user")
							.getString("username");
					String authoToken = responseJson.get(0).getJSONObject("auth").getString("token");
					new UserData(serviceEmail, serviceUsername, authoToken);

					Intent openCampusMap = new Intent(MapAccess.this, MapHandler.class);
					Bundle userInfo = new Bundle();
					userInfo.putBoolean("storeInfo", true);
					openCampusMap.putExtras(userInfo);
					openCampusMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
							Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(openCampusMap);
					finish();
				}else if(responseJson.get(responseJson.size()-1).getInt("status") == 
						HttpHandler.UNAUTHORIZED){
					Toast.makeText(getApplicationContext(), getResources()
							.getString(R.string.login_failure),Toast.LENGTH_SHORT).show();
				}else{
					setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING, action);
				}
			}catch(JSONException e) {
				e.printStackTrace();
			}
		}else if(action.equals(ACTION_REGISTER)){
			try{
				Log.i("responseJson", responseJson.toString());
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
					if(responseJson.get(0).getBoolean("success")){ 
						//Se comprueba que haya sido exitosa la conexi�n con el Servidor.

						returnToLoginView(null);  //No hay necesidad de enviar ning�n 
						//par�metro para que se ejecute la acci�n 
						//deseada.
						Toast.makeText(getApplicationContext(), getResources()
								.getString(R.string.register_success),
								Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getApplicationContext(), getResources()
								.getString(R.string.register_user_already_exists_failure),
								Toast.LENGTH_LONG).show();
					}
				}else{
					setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING, action);
				}
			}catch(JSONException e) {
				e.printStackTrace();
			}
		}

		paramsForHttpPost.clear();
	}

	//La acci�n se pone como final String para que pueda ser accedida desde el .setPositiveButton().
	public void setBuilder(String status, final String action){
		AlertDialog.Builder builder = new AlertDialog.Builder(MapAccess.this);
		builder.setTitle(getResources().getString(R.string.connection_error_title));
		builder.setMessage(getResources().getString(R.string.connection_error));

		builder.setPositiveButton(getResources().getString(R.string.retry), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(action.equals(ACTION_AUTH)){
					//Se intenta nuevamente la conexi�n con el servicio realizando 
					//otra vez el proceso desde logIn(). 
					//El par�metro es nulo porque en este caso no es requerido.
					logIn(null);
				}else if(action.equals(ACTION_REGISTER)){
					//Se intenta nuevamente la conexi�n con el servicio realizando 
					//otra vez el proceso desde register(). 
					//El par�metro es nulo porque en este caso no es requerido.
					register(null);
				}
			}
		});

		builder.setNegativeButton(getResources().getString(R.string.exit), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				exitApp();
			}
		});

		AlertDialog mapAccessAlertDialog = builder.create();
		mapAccessAlertDialog.show();
	}

	public void exitApp(){
		Intent exitApp = new Intent(MapAccess.this, MainActivity.class);
		Bundle userActionInfo = new Bundle();
		userActionInfo.putBoolean("exit", true);
		exitApp.putExtras(userActionInfo);
		exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(exitApp);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()) {
			menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
			menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
			menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
		}
		getMenuInflater().inflate(R.menu.map_access, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el men� como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del men� para setearlo correctamente.
		menuToShowIds.clear();	  //Tambi�n sus ids.

		menuToShow.add(getResources().getString(R.string.map));
		menuToShow.add(getResources().getString(R.string.places));
		menuToShow.add(getResources().getString(R.string.about_us));
		menuToShowIds.add(R.string.map);
		menuToShowIds.add(R.string.places);
		menuToShowIds.add(R.string.about_us);

		drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				android.R.id.text1, menuToShow));

		return true;
	}

}
