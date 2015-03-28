package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.helpers.HttpHandler;
import com.sicomeafit.campusmovil.models.MapData;
import com.sicomeafit.campusmovil.models.UserData;


public class MapHandler extends FragmentActivity implements SubscribedActivities,
OnCameraChangeListener, OnMarkerClickListener, OnMapLongClickListener, OnInfoWindowClickListener{  

	private GoogleMap campusMap;

	//Propiedades del mapa del campus.             y          x
	private final LatLng UniEafit = new LatLng(6.200696,-75.578433); //Encontrado en GoogleMaps.
	private int minZoom = 17;					
	private final LatLng bLCorner = new LatLng(6.1932748,-75.5823696);    
	private final LatLng tRCorner = new LatLng(6.203500,-75.577057);
	private final LatLngBounds campusMapBounds = new LatLngBounds(bLCorner, tRCorner);

	//Para usar SharedPreferences.
	private static final String USER_EMAIL = "USER_EMAIL";
	private static final String USERNAME = "USERNAME";
	private static final String USER_TOKEN = "USER_TOKEN";

	public static final int CLEAR_USER_DATA = -1;

	private ArrayList<Marker> fixedMarkersList = new ArrayList<Marker>();
	private ArrayList<Marker> markersListForQuerying = new ArrayList<Marker>();
	private Marker lastMarkerClicked;
	private boolean isLastMarkerAFixedMarker;

	//Contendrán la posición y el título de un marcador añadido por el usuario. 
	private LatLng userMarkerPosition;
	private String userMarkerTitle;		

	private HttpHandler httpHandler = new HttpHandler();
	private final String ACTION_MARKERS = "/markers";
	private final String ACTION_CREATE_USER_MARKER = "/create_user_marker";
	private Map<String, String> paramsForHttpPost = new HashMap<String, String>();

	//Se declara el SearchView y su respectivo EditText para utilizarlo luego y poder setear un String 
	//cuando se hace búsqueda por voz y un indicador si no se encuentran resultados.
	SearchView searchView = null;
	EditText searchViewEditText;

	//Navigation Drawer (menú lateral).
	ListView drawer = null;
	DrawerLayout drawerLayout = null;
	ActionBarDrawerToggle toggle = null;
	ArrayList<String> menuToShow = new ArrayList<String>(); 
	ArrayList<Integer> menuToShowIds = new ArrayList<Integer>(); 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_handler);
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		//Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
		httpHandler.addListeningActivity(this);

		Bundle paramsBag = getIntent().getExtras();
		if(paramsBag != null && paramsBag.getInt("actionCode") == CLEAR_USER_DATA){  
			//Llegó un aviso de acceso no autorizado para que se borren los datos del usuario 
			//o uno de log out.
			clearSharedPreferences();	
			if(paramsBag.getBoolean("isActivityForResult")){
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent); 
				finish();
			}else{ //Simplemente se hizo log out y se quiere mostrar el mapa.
				setMapView();
				setGeneralListeners();
			}
		}else{
			setMapView();
			setGeneralListeners();
		}
	}

	/*
	 * Código de apoyo: http://creandoandroid.es/implementar-navigation-drawer-menu-lateral/
	 */
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

	public void setMapView(){
		campusMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		//Con el fin de comprobar si el dispositivo cuenta con Google Play Services.
		int isEnabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isEnabled != ConnectionResult.SUCCESS) {
			//Esto imprimiría en LogCat un mensaje de error por no presencia de Google Play Services.
			GooglePlayServicesUtil.getErrorDialog(isEnabled, this, 0);
		}

		campusMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		campusMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));

		campusMap.setMyLocationEnabled(true);
		//Se comprueba sí es necesario que el usuario aparezca en el mapa.
		Location userLocation = campusMap.getMyLocation();
		if(userLocation != null){
			if(!campusMapBounds.contains(new LatLng(userLocation.getLatitude(), 
					userLocation.getLongitude()))){
				campusMap.setMyLocationEnabled(false);
			}
		}

		campusMap.getUiSettings().setZoomControlsEnabled(false);
		campusMap.getUiSettings().setCompassEnabled(true);

		Bundle paramsBag = getIntent().getExtras();  //	Aquí estarían los parámetros recibidos.
		if(paramsBag != null && paramsBag.getBoolean("storeInfo")){  //La app viene del log in.
			clearSharedPreferences();
			saveSharedPreferences();
		}else{
			checkUserData();
		}

		if(httpHandler.isInternetConnectionAvailable(this)){
			httpHandler.sendRequest(HttpHandler.API_V1, ACTION_MARKERS, "?auth=" + UserData.getToken(),
					paramsForHttpPost, new HttpGet(), MapHandler.this);
		}else{
			Toast.makeText(getApplicationContext(), getResources()
					.getString(R.string.internet_connection_required), Toast.LENGTH_SHORT).show();
		}
	}

	public void setGeneralListeners(){
		campusMap.setOnCameraChangeListener(this);
		campusMap.setOnMarkerClickListener(this);
		campusMap.setOnMapLongClickListener(this);
		campusMap.setOnInfoWindowClickListener(this);  
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		Vibrator outOfBoundsVb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if(!campusMapBounds.contains(new LatLng(position.target.latitude, 
				position.target.longitude)) || position.zoom < minZoom){  //El usuario está tratando de
			//alejarse demasiado del mapa
			//del campus.

			long[] vbPattern = {0, 250, 250, 250};  //{sleep, vibrate, sleep, vibrate...}.
			outOfBoundsVb.vibrate(vbPattern, -1);   //Vibra según el patrón de vibración para luego 
			//retornar al usuario a la vista inicial del 
			//mapa del campus.
			campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
		}

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if(lastMarkerClicked == null){
			boolean isMarkerAFixedMarker = false;
			for(int i = 0; i < fixedMarkersList.size() && !isMarkerAFixedMarker; i++){
				if(marker.getId().equals(fixedMarkersList.get(i).getId())){
					isMarkerAFixedMarker = true;
				}
			}
			lastMarkerClicked = marker;
			if(isMarkerAFixedMarker){
				isLastMarkerAFixedMarker = true;
			}else{
				isLastMarkerAFixedMarker = false;
				//Se hace explícito, debido a que es un marcador propio y no tiene por defecto esa
				//funcionalidad.
				marker.showInfoWindow();
			}
			return false;
		}else{
			boolean isMarkerAFixedMarker = false;
			for(int i = 0; i < fixedMarkersList.size() && !isMarkerAFixedMarker; i++){
				if(marker.getId().equals(fixedMarkersList.get(i).getId())){
					isMarkerAFixedMarker = true;
				}
			}
			if(lastMarkerClicked.equals(marker)){
				marker.hideInfoWindow();
				lastMarkerClicked = null;
				return true;
			}
			lastMarkerClicked = null;
			if(isMarkerAFixedMarker){
				isLastMarkerAFixedMarker = true;
			}else{
				isLastMarkerAFixedMarker = false;
			}
			return false;
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		String windowTitle = marker.getTitle();
		String windowSubtitle = marker.getSnippet();
		if(isLastMarkerAFixedMarker){
			goToSelectedPlace(windowTitle, windowSubtitle);
		}else{
			manageUserMarker(marker.getPosition(), marker.getTitle(), MapHandler.this);
		}
	}

	@Override
	public void onMapLongClick(LatLng pressedPoint) {
		if(UserData.getToken() != null){
			if(campusMapBounds.contains(new LatLng(pressedPoint.latitude, pressedPoint.longitude))){
				//Es posible poner un marker en el punto presionado.	
				//Se muestra un AlertDialog que pide el título del marcador para poderlo agregar.
				userMarkerPosition = pressedPoint;
				setBuilder(ACTION_CREATE_USER_MARKER, null);
			}
		}
	}

	public static void manageUserMarker(LatLng position, String title, Context context){
		Intent goToUserMarkersManager = new Intent(context, UserMarkersManager.class);
        goToUserMarkersManager.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		Bundle paramsBag = new Bundle();
		paramsBag.putDouble("markerLat", position.latitude);
		paramsBag.putDouble("markerLong", position.longitude);
		paramsBag.putString("markerTitle", title);
		goToUserMarkersManager.putExtras(paramsBag);
		context.startActivity(goToUserMarkersManager);
	}

	public void saveSharedPreferences(){
		//Se almacena el token del usuario para ver si es necesario
		//solicitarle que se haga nuevamente log in o no.
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(USER_EMAIL, UserData.getEmail());
		editor.putString(USERNAME, UserData.getUsername());
		editor.putString(USER_TOKEN, UserData.getToken());
		editor.commit();	
	}

	public void clearSharedPreferences(){
		//Se eliminan los datos almacenados pues se requiere un nuevo token.
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear().commit();

		Bundle paramsBag = getIntent().getExtras();
		if(paramsBag != null && paramsBag.getInt("actionCode") == CLEAR_USER_DATA){
			//Se setean a null los datos del usuario dentro de la clase UserData para simular
			//un log out.
			UserData.setEmail(null);
			UserData.setUsername(null);
			UserData.setToken(null);
		}	
	}

	public void checkUserData(){
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String userEmail = prefs.getString(USER_EMAIL, null);
		String username = prefs.getString(USERNAME, null);
		String userToken = prefs.getString(USER_TOKEN, null); //Retorna null si el token no existe.

		//El usuario ingreso hace poco o no ha cerrado sesión.
		if(userEmail != null && username != null && userToken != null){ 
			new UserData(userEmail, username, userToken); 
		}
	}

	public void goToSelectedPlace(String windowTitle, String windowSubtitle){
		Intent openBuildingInfo = new Intent(MapHandler.this, InformationManager.class);
        openBuildingInfo.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		Bundle windowInformation = new Bundle();                                                          
		windowInformation.putString("windowTitle", windowTitle);
		windowInformation.putString("windowSubtitle", windowSubtitle);
		openBuildingInfo.putExtras(windowInformation);
		startActivity(openBuildingInfo); 
	}

	public void logout(){
		Intent logOut = new Intent(MapHandler.this, MapHandler.class);
		Bundle actionCode = new Bundle();
		actionCode.putInt("actionCode", CLEAR_USER_DATA);
		logOut.putExtras(actionCode);
		logOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(logOut);
		finish();
	}

	@Override  //Se utiliza para sincronizar el estado del Navigation Drawer (menú lateral).
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected void onNewIntent(Intent intent) {
		ArrayList<Marker> queryMarkers = new ArrayList<Marker>();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			String queryFirstUpper = query.substring(0, 1).toUpperCase() + query.substring(1);
			searchView.setQuery(queryFirstUpper, false);
			String newText = query.trim();
			//No es necesario asignarlo a queryMarkers pues ya su lógica está ocurriendo en
			//getQueryMarkers();
			getQueryMarkers(newText, queryMarkers);
		}
	}

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.my_markers:
			openSelectedItem = new Intent(MapHandler.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(MapHandler.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.log_in:
			openSelectedItem = new Intent(MapHandler.this, MapAccess.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.suggestions:
			openSelectedItem = new Intent(MapHandler.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(MapHandler.this, AboutUs.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
		case R.id.myMarkers:
			openSelectedItem = new Intent(MapHandler.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.places:
			openSelectedItem = new Intent(MapHandler.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.login:
			openSelectedItem = new Intent(MapHandler.this, MapAccess.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.suggestions:
			openSelectedItem = new Intent(MapHandler.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.aboutUs:
			openSelectedItem = new Intent(MapHandler.this, AboutUs.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.logout:
			logout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		startActivity(openSelectedItem);
		return true;
	}

	@Override
	public void notify(String action, ArrayList<JSONObject> responseJson) {
		if(action.equals(ACTION_MARKERS)){
			try {
				Log.i("responseJson", responseJson.toString());
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
					ArrayList<String> markersTitles = new ArrayList<String>();
					ArrayList<String> markersSubtitles = new ArrayList<String>();
					ArrayList<String> markersCategories = new ArrayList<String>();
					Map<LatLng, String> userMarkers = new HashMap<LatLng, String>();
                    Map<LatLng, Integer> userMarkersIds = new HashMap<LatLng, Integer>();
					for(int i = 0; i < responseJson.size()-1; i++){
						double latitude = responseJson.get(i).getDouble("latitude");
						double longitude = responseJson.get(i).getDouble("longitude");
						String title = responseJson.get(i).getString("title");
						//Este sería el subtítulo.	
						String snippet = responseJson.get(i).getString("subtitle"); 
						String category = responseJson.get(i).getString("category");
                        int id = responseJson.get(i).getInt("id");
						if(!category.equals(UserMarkers.USER_MARKER_CATEGORY)){
							Marker fixedMarker = campusMap.addMarker(new MarkerOptions()
							.position(new LatLng(latitude, 
									longitude))
									.title(title)
									.snippet(snippet) 
									.icon(BitmapDescriptorFactory
											.fromResource
											(R.drawable.map_marker)));
							markersTitles.add(fixedMarker.getTitle());
							markersSubtitles.add(fixedMarker.getSnippet());
							markersCategories.add(category);
							fixedMarkersList.add(fixedMarker);
							markersListForQuerying.add(fixedMarker);
						}else{
							Marker userMarker = campusMap.addMarker(new MarkerOptions()
							.position(new LatLng(latitude, longitude))
							.title(title) 
							.snippet("")
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.user_marker)));
							//Con esto se almacenan los marcadores del usuario localmente.
							userMarkers.put(new LatLng(latitude, longitude), title);
                            userMarkersIds.put(new LatLng(latitude, longitude), id);
							markersListForQuerying.add(userMarker);
						}
					}
					new MapData(markersTitles, markersSubtitles, markersCategories, userMarkers,
                                userMarkersIds);
					//Se guardan los títulos, subtítulos y categorías de los markers en la clase MapData.
					String username = "";
					if(UserData.getUsername() != null){ //Si el usuario no estaba loggeado,
						//entonces no se incluye en el mensaje de
						//bienvenida.
						username = UserData.getUsername();
					}
					Toast.makeText(getApplicationContext(), getResources()
							.getString(R.string.welcome_msg) + " " + username + "!", 
							Toast.LENGTH_LONG).show();
				}else{
					if(responseJson.get(responseJson.size()-1).getInt("status") == 
							HttpHandler.UNAUTHORIZED){
						setBuilder(HttpHandler.UNAUTHORIZED_STRING, action);
					}else{
						setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING, action);
					}
				}
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}else if(action.equals(ACTION_CREATE_USER_MARKER)){
			try {
				Log.i("responseJson", responseJson.toString());
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
					if(responseJson.get(0).getBoolean("success")){
						userMarkerPosition = null;
						userMarkerTitle = "";
						double latitude = responseJson.get(0).getJSONObject("marker").getDouble("latitude");
						double longitude = responseJson.get(0).getJSONObject("marker")
								.getDouble("longitude");
						String title = responseJson.get(0).getJSONObject("marker").getString("title");
                        int id = responseJson.get(0).getJSONObject("marker").getInt("id");
						campusMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
								.title(title)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.user_marker)));
						//Se adiciona el marcador del usuario localmente para que pueda ser accedido 
						//usando el SearchWidget.
						MapData.addUserMarker(new LatLng(latitude, longitude), title);
                        MapData.addUserMarkerId(new LatLng(latitude, longitude), id);
						Toast.makeText(getApplicationContext(), title + " " + 
								getResources().getString(R.string.marker_successfully_created), 
								Toast.LENGTH_SHORT).show();
					}else{
						//Se muestra un diálogo para reintentar o cancelar la creación del nuevo marker.
						setBuilder(HttpHandler.NOT_SUCCEEDED_STRING, action);
					}
				}else{
					if(responseJson.get(responseJson.size()-1).getInt("status") == 
							HttpHandler.UNAUTHORIZED){
						setBuilder(HttpHandler.UNAUTHORIZED_STRING, action);
					}else{
						setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING, action);
					}
				}
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}

		paramsForHttpPost.clear();
	} 

	//La acción se pone como final String para que pueda ser accedida desde el .setPositiveButton().
	//El parámetro pressedPoint solamente es necesario para la creación de un marcador de usuario.
	public void setBuilder(String status, final String action){
		AlertDialog.Builder builder = new AlertDialog.Builder(MapHandler.this);
		final EditText etMarkerTitle = new EditText(this);

		switch(status){
		case HttpHandler.NOT_SUCCEEDED_STRING:
			builder.setTitle(getResources().getString(R.string.oops));
			builder.setMessage(getResources().getString(R.string.marker_not_created));

			builder.setPositiveButton(getResources().getString(R.string.retry), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se intenta nuevamente la conexión con el servicio realizando 
					//otra vez el proceso de createUserMarker(). 
					createUserMarker();
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					userMarkerPosition = null;
					userMarkerTitle = "";
				}
			});

			break;
		case HttpHandler.UNAUTHORIZED_STRING:
			builder.setTitle(getResources().getString(R.string.log_in));
			builder.setMessage(getResources().getString(R.string.login_needed_1));

			builder.setPositiveButton(getResources().getString(R.string.log_in), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se hace un startActivityForResult para que se borren
					//los datos del usuario. Luego se pasa al Log in.
					Intent clearUserData = new Intent(MapHandler.this, MapHandler.class);
					Bundle actionCode = new Bundle();
					actionCode.putInt("actionCode", CLEAR_USER_DATA);
					actionCode.putBoolean("isActivityForResult", true);
					clearUserData.putExtras(actionCode);
					startActivityForResult(clearUserData, 1);
					//El 1 indica que cuando la actividad finalice, retornará a
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
					if(action.equals(ACTION_MARKERS)){
						httpHandler.sendRequest(HttpHandler.API_V1, ACTION_MARKERS, "?auth=" + 
								UserData.getToken(), paramsForHttpPost, new HttpGet(), 
								MapHandler.this);
					}else if(action.equals(ACTION_CREATE_USER_MARKER)){
						createUserMarker();
					}
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.exit), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitApp();
				}
			});

			break;
		case ACTION_CREATE_USER_MARKER:
			etMarkerTitle.setHint(getResources().getString(R.string.enter_marker_name));
			etMarkerTitle.setSingleLine(true);
			etMarkerTitle.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			builder.setIcon(getResources().getDrawable(R.drawable.user_marker));
			builder.setTitle(getResources().getString(R.string.new_user_marker));
			builder.setMessage(getResources().getString(R.string.give_marker_name));
			builder.setView(etMarkerTitle);

			builder.setPositiveButton(getResources().getString(R.string.create), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					userMarkerTitle = etMarkerTitle.getText().toString();
					createUserMarker();
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					userMarkerPosition = null;
					userMarkerTitle = "";
				}
			});

			break;
		}

		final AlertDialog mapHandlerAlertDialog = builder.create();
		mapHandlerAlertDialog.show();

		if(status.equals(ACTION_CREATE_USER_MARKER)){
			mapHandlerAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

			//Solamente permite agregar un marcador al usuario cuando contiene un título.
			etMarkerTitle.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					if(etMarkerTitle.getText().toString().trim().isEmpty()) {
						mapHandlerAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					}else{
						mapHandlerAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
					}
				}
			});
		}
	}

	public void createUserMarker(){
		paramsForHttpPost.put("latitude", String.valueOf(userMarkerPosition.latitude));
		paramsForHttpPost.put("longitude", String.valueOf(userMarkerPosition.longitude));
		paramsForHttpPost.put("title", userMarkerTitle);

        if(httpHandler.isInternetConnectionAvailable(this)){
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_CREATE_USER_MARKER, "?auth=" +
                    UserData.getToken(), paramsForHttpPost, new HttpPost(), MapHandler.this);
        }else{
            Toast.makeText(getApplicationContext(), getResources()
                    .getString(R.string.internet_connection_required), Toast.LENGTH_SHORT).show();
        }
	}

	public void exitApp(){
		Intent exitApp = new Intent(MapHandler.this, MainActivity.class);
		Bundle userActionInfo = new Bundle();
		userActionInfo.putBoolean("exit", true);
		exitApp.putExtras(userActionInfo);
		exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(exitApp);
		finish();
	}

	public void handleSearchWidget(){
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {

			@Override
			public boolean onClose() {
				for(int k = 0; k < markersListForQuerying.size(); k++){
					Marker marker = markersListForQuerying.get(k);
					marker.setVisible(true);
				}

				//Se pone la barra de búsqueda en su estado original.
				if(searchViewEditText != null){
					searchViewEditText.setError(null);
				}

				campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
				return false;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			ArrayList<Marker> queryMarkers = new ArrayList<Marker>();

			@Override
			public boolean onQueryTextSubmit(String query) {

				if(queryMarkers.size() == 1){  //Quiere decir que hay un único marker visible.
					campusMap.animateCamera(CameraUpdateFactory
							.newLatLngZoom(queryMarkers.get(0).getPosition(), minZoom));
				}else{
					campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
				}

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newTextNoTrim) {
				String newText = newTextNoTrim.trim();
				queryMarkers = getQueryMarkers(newText, queryMarkers);

				return false;
			}
		});
	}

	@SuppressLint("DefaultLocale")
	public ArrayList<Marker> getQueryMarkers(String query, ArrayList<Marker> queryMarkers){
		for(int i = 0; i < markersListForQuerying.size(); i++){
			Marker marker = markersListForQuerying.get(i);
			marker.setVisible(false);
		}
		if (query != null && query.length() != 0) {
			queryMarkers = new ArrayList<Marker>();
			for (Marker m : markersListForQuerying) {
				String titleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
						.replaceAll("í", "i").replaceAll("ó", "o")
						.replaceAll("ú", "u");
				String subtitleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
						.replaceAll("í", "i").replaceAll("ó", "o")
						.replaceAll("ú", "u");

				if (m.getTitle().toLowerCase().contains(query.toLowerCase())
						|| m.getSnippet().toLowerCase().contains(query.toLowerCase())
						|| titleWithoutSpCh.toLowerCase().contains(query.toLowerCase()) 
						|| subtitleWithoutSpCh.toLowerCase().contains(query.toLowerCase())){
					queryMarkers.add(m);
				}
			}

			if(queryMarkers.size() != 0){

				//Se pone la barra de búsqueda en su estado original.
				if(searchViewEditText != null){
					searchViewEditText.setError(null);
				}

				for(int j = 0; j < queryMarkers.size(); j++){
					Marker queryMarker = queryMarkers.get(j);
					queryMarker.setVisible(true);
				}

				//En caso de único resultado, se lleva al usuario hacia el marcador.
				if(queryMarkers.size() == 1){
					campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(queryMarkers.get(0)
							.getPosition(),
							minZoom));
				}else{
					//Si no se encontraron marcadores asociados o no es único el resultado, se muestra
					//toda la Universidad.
					campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
				}

			}else{
				//Se cambia el estado de la barra de búsqueda, pues no se encontraron resultados.
				if(searchViewEditText != null){
					searchViewEditText.setError(getResources()
							.getString(R.string.no_results_found));
				}

				campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));

			}
		}else{
			//Se pone la barra de búsqueda en su estado original.
			if(searchViewEditText != null){
				searchViewEditText.setError(null);
			}

			//Si no hay texto a buscar, todos los markers son visibles.
			for(int k = 0; k < markersListForQuerying.size(); k++){
				Marker marker = markersListForQuerying.get(k);
				marker.setVisible(true);
			}
			campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
		}

		return queryMarkers;	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			if (UserData.getToken() == null){  //El usuario no está loggeado.
				menu.add(0, R.id.places, Menu.FIRST+1, getResources().getString(R.string.places));
				menu.add(0, R.id.login, Menu.FIRST+2, getResources()
						.getString(R.string.log_in));
				menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
			}else{
				menu.add(0, R.id.myMarkers, Menu.FIRST+1, getResources().getString(R.string.my_markers));
				menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
				menu.add(0, R.id.suggestions, Menu.FIRST+3, getResources().getString(R.string.suggestions));
				menu.add(0, R.id.aboutUs, Menu.FIRST+4, getResources().getString(R.string.about_us));
				menu.add(0, R.id.logout, Menu.FIRST+5, getResources().getString(R.string.log_out));
			}
		}
		getMenuInflater().inflate(R.menu.map_handler, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el menú como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.

		if (UserData.getToken() == null){  //El usuario no está loggeado.
			menuToShow.add(getResources().getString(R.string.places));
			menuToShow.add(getResources().getString(R.string.log_in));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShowIds.add(R.string.places);
			menuToShowIds.add(R.string.log_in);
			menuToShowIds.add(R.string.about_us);
		}else{
			menuToShow.add(getResources().getString(R.string.my_markers));
			menuToShow.add(getResources().getString(R.string.places));
			menuToShow.add(getResources().getString(R.string.suggestions));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShow.add(getResources().getString(R.string.log_out));
			menuToShowIds.add(R.string.my_markers);
			menuToShowIds.add(R.string.places);
			menuToShowIds.add(R.string.suggestions);
			menuToShowIds.add(R.string.about_us);
			menuToShowIds.add(R.string.log_out);
		}
		//Se setea el menú de acuerdo al estado del usuario.
		drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				android.R.id.text1, menuToShow));

		// Se agrega el SearchWidget.
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.options_menu_main_search).getActionView();

		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		int searchViewEditTextId = searchView.getContext().getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		searchViewEditText = (EditText) searchView.findViewById(searchViewEditTextId);

		handleSearchWidget();

		return true;
	}

}
