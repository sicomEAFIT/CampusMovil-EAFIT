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
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
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
import android.widget.Toast;


public class Suggestions extends Activity implements SubscribedActivities {

	private EditText etSuggestion;
	private String suggestion;

	private HttpHandler httpHandler = new HttpHandler();
	private final String ACTION = "/comment";
	private Map<String, String> paramsForHttpPost = new HashMap<String, String>();

	//Navigation Drawer (menú lateral).
	ListView drawer = null;
	DrawerLayout drawerLayout = null;
	ActionBarDrawerToggle toggle = null;
	ArrayList<String> menuToShow = new ArrayList<String>(); 
	ArrayList<Integer> menuToShowIds = new ArrayList<Integer>(); 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestions);
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		initViewElements();
		//Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
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

		toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.app_name, 
				R.string.app_name ){

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

	public void initViewElements(){
		etSuggestion = (EditText) findViewById(R.id.suggestion);
	}

	public void makeSuggestion(View v){
		suggestion = etSuggestion.getText().toString();
		if(suggestion.trim().isEmpty()){
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.suggestion_empty),
					Toast.LENGTH_SHORT).show();
		}else{
			paramsForHttpPost.put("email", UserData.getEmail());
			paramsForHttpPost.put("username", UserData.getUsername());
			paramsForHttpPost.put("suggestion", suggestion);
			paramsForHttpPost.put("suggestionUTF", convertToUTF8(suggestion));
			if(httpHandler.isInternetConnectionAvailable(this)){
				//Se envía la petición al HttpHandler.
				httpHandler.sendRequest(HttpHandler.API_V1, ACTION, "?auth=" + UserData.getToken(), 
						paramsForHttpPost, new HttpPost(), Suggestions.this);
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

	public void logout(){
		Intent logOut = new Intent(Suggestions.this, MapHandler.class);
		Bundle actionCode = new Bundle();
		actionCode.putInt("actionCode", MapHandler.CLEAR_USER_DATA);
		logOut.putExtras(actionCode);
		logOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(logOut);
		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if(resultCode == RESULT_OK){    //Ya se borró la info. del usuario y se puede proceder
				//a una nueva autenticación.
				Intent returnToLogin = new Intent(Suggestions.this, MapAccess.class);
				Bundle statusInfo = new Bundle();
				statusInfo.putInt("status", HttpHandler.UNAUTHORIZED);
				returnToLogin.putExtras(statusInfo);
				returnToLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
						Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(returnToLogin);
				finish();         
			}

		}

	} 

	@Override  //Se utiliza para sincronizar el estado del Navigation Drawer (menú lateral).
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.map:
			openSelectedItem = new Intent(Suggestions.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(Suggestions.this, Places.class); 
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(Suggestions.this, AboutUs.class); 
			break;
		case R.string.log_out:
			logout();
			return;
		}
		startActivity(openSelectedItem);
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (toggle.onOptionsItemSelected(item)) {
			return true; //Hace que se abra el menú lateral al presionar el ícono.
		}
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
			logout();
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
	public void notify(String action, ArrayList<JSONObject> responseJson) {
		try {
			Log.i("responseJson", responseJson.toString());
			if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
				if(responseJson.get(0).getBoolean("success")){ 
					etSuggestion.setText("");
					Toast.makeText(getApplicationContext(), getResources()
							.getString(R.string.suggestion_sent), Toast.LENGTH_LONG).show();
					Intent openMap = new Intent(Suggestions.this, MapHandler.class); 
					openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(openMap);
				}else{
					//Se muestra un diálogo para reintentar o cancelar el envío de la sugerencia.
					setBuilder(HttpHandler.NOT_SUCCEEDED_STRING);
				}
			}else{
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.UNAUTHORIZED){
					setBuilder(HttpHandler.UNAUTHORIZED_STRING);
				}else{
					setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING);
				}
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}

		paramsForHttpPost.clear();
	}

	//Este método no requiere recibir la acción dado que en esta actividad se ejecuta un único servicio.
	public void setBuilder(String status){
		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestions.this);
		switch(status){
		case HttpHandler.NOT_SUCCEEDED_STRING:
			builder.setTitle(getResources().getString(R.string.oops));
			builder.setMessage(getResources().getString(R.string.suggestion_not_sent));

			builder.setPositiveButton(getResources().getString(R.string.retry), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se intenta nuevamente la conexión con el servicio realizando 
					//otra vez el proceso desde makeSuggestion(). 
					//El parámetro es nulo porque en este caso no es requerido.
					makeSuggestion(null);
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					etSuggestion.setText("");
					Intent openMap = new Intent(Suggestions.this, MapHandler.class); 
					openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(openMap);
				}
			});

			break;
		case HttpHandler.UNAUTHORIZED_STRING:
			builder.setTitle(getResources().getString(R.string.log_in));
			builder.setMessage(getResources().getString(R.string.login_needed_2));

			builder.setPositiveButton(getResources().getString(R.string.log_in), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se hace un startActivityForResult para que se borren
					//los datos del usuario. Luego se pasa al Log in.
					Intent clearUserData = new Intent(Suggestions.this, MapHandler.class);
					Bundle actionCode = new Bundle();
					actionCode.putInt("actionCode", MapHandler.CLEAR_USER_DATA);
					actionCode.putBoolean("isActivityForResult", true);
					clearUserData.putExtras(actionCode);
					startActivityForResult(clearUserData, 1);
					//El 1 indica que cuando la actividad finalice, retornara a
					//onActivityResult de esta actividad.
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.exit), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitApp();
				}
			});

			break;
		case HttpHandler.SERVER_INTERNAL_ERROR_STRING:
			builder.setTitle(getResources().getString(R.string.connection_error_title));
			builder.setMessage(getResources().getString(R.string.connection_error));

			builder.setPositiveButton(getResources().getString(R.string.retry), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se intenta nuevamente la conexión con el servicio realizando 
					//otra vez el proceso desde makeSuggestion(). 
					//El parámetro es nulo porque en este caso no es requerido.
					makeSuggestion(null);
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.exit), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitApp();
				}
			});

			break;
		}

		AlertDialog suggestionAlertDialog = builder.create();
		suggestionAlertDialog.show();
	}

	public void exitApp(){
		Intent exitApp = new Intent(Suggestions.this, MainActivity.class);
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
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
			menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
			menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
			menu.add(0, R.id.logout, Menu.FIRST+4, getResources().getString(R.string.log_out));
		}
		getMenuInflater().inflate(R.menu.suggestions, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el menú como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.

		menuToShow.add(getResources().getString(R.string.map));
		menuToShow.add(getResources().getString(R.string.places));
		menuToShow.add(getResources().getString(R.string.about_us));
		menuToShow.add(getResources().getString(R.string.log_out));
		menuToShowIds.add(R.string.map);
		menuToShowIds.add(R.string.places);
		menuToShowIds.add(R.string.about_us);
		menuToShowIds.add(R.string.log_out);

		drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				android.R.id.text1, menuToShow));

		return true;
	}

}
