package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
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
import android.widget.SearchView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapHandler extends FragmentActivity implements OnCameraChangeListener, 
															OnMarkerClickListener, 
															OnInfoWindowClickListener,
															OnMapClickListener{  
	
	private GoogleMap campusMap;
	private Marker lastMarkerClicked;
	//Propiedades del mapa del campus.             y          x
	private final LatLng UniEafit = new LatLng(6.200696,-75.578433); //Encontrado en GoogleMaps.
	private int minZoom = 17;					
	private final LatLng bLCorner = new LatLng(6.1932748,-75.5823696);    
	private final LatLng tRCorner = new LatLng(6.203500,-75.577057);
	private final LatLngBounds campusMapBounds = new LatLngBounds(bLCorner, tRCorner);
	//
	private ArrayList<Marker> fixedMarkersList = new ArrayList<Marker>();
	//private String POST_URL = "http://www.profe5.com/sicom_trash/Android/handleMainOrUserMarkers.php";
	private String POST_URL = "";
	private String wantedService;
	private HashMap<String, String> paramsForHttpPOST = new HashMap<String, String>();
	
	//Para usar SharedPreferences.
	private static final String USER_EMAIL = "USER_EMAIL";
	private static final String USERNAME = "USERNAME";
	private static final String USER_TOKEN = "USER_TOKEN";
	
	//Códigos HTTP.
	private static final int SUCCESS = 200;
	private static final int UNAUTHORIZED = 401;
	
	private static final int CLEAR_USER_DATA = -1;
	
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
	
	public void setMapView(){
		campusMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	            	.getMap();
		
		//Con el fin de comprobar si el dispositivo cuenta con Google Play Services.
		if (campusMap != null) {
		    // Setup your map...
		} else {
		    int isEnabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		    if (isEnabled != ConnectionResult.SUCCESS) {
		        GooglePlayServicesUtil.getErrorDialog(isEnabled, this, 0);
		    }
		}
		//
		
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
		if(paramsBag != null && paramsBag.getBoolean("storeInfo")){  //La app viene del Log in.
			clearSharedPreferences();
			saveSharedPreferences();
		}else{
			checkUserData();
		}
		if(isInternetConnectionAvailable()){
			wantedService = "Add main markers";
			POST_URL = "http://campusmovilapp.herokuapp.com/api/v1/markers";
												   //Para dar autorización al acceso de los marcadores.
			new POSTConnection().execute(POST_URL);
		}else{
			Toast.makeText(getApplicationContext(), getResources()
				           .getString(R.string.internet_connection_required), 
				           Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setGeneralListeners(){
        campusMap.setOnMarkerClickListener(this);
        campusMap.setOnCameraChangeListener(this);
        campusMap.setOnInfoWindowClickListener(this);
        campusMap.setOnMapClickListener(this);   
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		Vibrator outOfBoundsVb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	if(!campusMapBounds.contains(new LatLng(position.target.latitude, 
    	   position.target.longitude)) || position.zoom < minZoom){  //El usuario está tratando de
    																//alejarse demasiado del mapa
    															   //del campus.
    		    
    		long[] vbPattern = {0, 250, 250, 250};  //{sleep, vibrate, sleep, vibrate...}.
    		outOfBoundsVb.vibrate(vbPattern, -1);  //Vibra según el patrón de vibración para luego 
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
			if(isMarkerAFixedMarker){
				lastMarkerClicked = marker;
				return false;
			}else{
				marker.remove();
				return true;
			}
		}else{
			boolean isMarkerAFixedMarker = false;
			for(int i = 0; i < fixedMarkersList.size() && !isMarkerAFixedMarker; i++){
				if(marker.getId().equals(fixedMarkersList.get(i).getId())){
					isMarkerAFixedMarker = true;
				}
			}
			if(isMarkerAFixedMarker){
				if(lastMarkerClicked.equals(marker)){
					marker.hideInfoWindow();
					lastMarkerClicked = null;
					return true;
				}
				return false;
			}else{
				marker.remove();
				lastMarkerClicked = null;
				return true;
			}
			
		}
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		String windowTitle = marker.getTitle();
		String windowSubtitle = marker.getSnippet();
		goToSelectedPlace(windowTitle, windowSubtitle);
	}
	
	@Override
	public void onMapClick(LatLng clickedPoint) {
		if(UserData.getToken() != null){
			if(campusMapBounds.contains(new LatLng(clickedPoint.latitude, clickedPoint.longitude))){
				   //Es posible poner un marker en el punto clickeado.	
			    	campusMap.addMarker(new MarkerOptions().position(clickedPoint)
			    						.icon(BitmapDescriptorFactory
			    						.fromResource(R.drawable.user_marker)));
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
			//un Log out.
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
    	Bundle windowInformation = new Bundle();                                                          
		windowInformation.putString("windowTitle", windowTitle);
		windowInformation.putString("windowSubtitle", windowSubtitle);
		openBuildingInfo.putExtras(windowInformation);
        startActivity(openBuildingInfo); 
	}
	
	 private class POSTConnection extends AsyncTask<String, Void, ArrayList<JSONObject>>{
		 
		private final ProgressDialog progressDialog = new ProgressDialog(MapHandler.this);
		private String url;
		private ArrayList<JSONObject> responseJSON = null;
		
		protected void onPreExecute() {
			if(wantedService.equals("Add main markers")){
				this.progressDialog.setMessage(getString(R.string.loading_map_data)); 
			}
            this.progressDialog.show();
         }
		 
        protected ArrayList<JSONObject> doInBackground(String...urls) {
        	url = urls[0];
     		HttpHandler httpHandler = new HttpHandler();
     		responseJSON = httpHandler.getInformation(url, wantedService, paramsForHttpPOST, 
     												  new HttpGet(url));
     		
     		return responseJSON;
         }

         protected void onPostExecute(ArrayList<JSONObject> responseJSON) {        	 
        	if(wantedService.equals("Add main markers")){
				try {
				    Log.i("responseJSON", responseJSON.toString());
				    if(responseJSON.get(responseJSON.size()-1).getInt("status") == SUCCESS){
				    	ArrayList<String> markersTitles = new ArrayList<String>();
					    ArrayList<String> markersSubtitles = new ArrayList<String>();
					    ArrayList<String> markersCategories = new ArrayList<String>();
						for(int i = 0; i < responseJSON.size()-1; i++){
							double latitude = responseJSON.get(i).getDouble("latitude");
							double longitude = responseJSON.get(i).getDouble("longitude");
							String title = responseJSON.get(i).getString("title");
							String snippet = responseJSON.get(i).getString("subtitle");  //Este sería 
																						//el 
																					   //subtítulo.	
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
					    	markersCategories.add(responseJSON.get(i).getString("category"));
					    	fixedMarkersList.add(fixedMarker);
						}
						new MapData(markersTitles, markersSubtitles, markersCategories); 
																	  //Se guardan los títulos, 
																	 //subtítulos y categorías de los 
																	//markers en la clase MapData.
						progressDialog.dismiss();
						String username = "";
						if(UserData.getUsername() != null){
							username = UserData.getUsername();
						}
						Toast.makeText(getApplicationContext(), getResources()
									   .getString(R.string.welcome_msg) + " " + username + "!", 
									   Toast.LENGTH_LONG).show();
				    }else{
						/*
						Toast.makeText(getApplicationContext(), getResources()
							       	   .getString(R.string.connection_error),Toast.LENGTH_SHORT)
							       	   .show();
						*/
						AlertDialog.Builder builder = new AlertDialog.Builder(MapHandler.this);
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
								Intent exitApp = new Intent(MapHandler.this, MainActivity.class);
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
				}catch (JSONException e) {
					progressDialog.dismiss();
					e.printStackTrace();
				}
        	}else if(wantedService.equals("Add user marker")){
				/*
				try{
					Log.i("responseJSON", responseJSON.toString());
					if(responseJSON.getString("username") != "null"){ //Se compara con el String 
					"null"
																	 //debido a que ya ha sido 
																	 // convertido
							 										//por el 
																	//convertInputStreamToString().
						returnToLoginView(null);  //No hay necesidad de enviar ningún parámetro
												 //para que se ejecute la acción deseada.
						Toast.makeText(getApplicationContext(), 
								       getResources().getString(R.string.register_success), 
								       Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getApplicationContext(), 
							           getResources().getString(
							           R.string.register_user_already_exists_failure),
							           Toast.LENGTH_LONG).show();
					}
					
				}catch(JSONException e) {
					e.printStackTrace();
				}
				*/
        	}
        	paramsForHttpPOST.clear();
         }
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
				for(int i = 0; i < fixedMarkersList.size(); i++){
					Marker marker = fixedMarkersList.get(i);
					marker.setVisible(false);
				}
				if (newText != null && newText.length() != 0) {
					queryMarkers = new ArrayList<Marker>();
					for (Marker m : fixedMarkersList) {
	                	String titleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
	                     	   					  			  .replaceAll("í", "i").replaceAll("ó", "o")
	                     	   					  			  .replaceAll("ú", "u");
	                	String subtitleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
					  			  								 .replaceAll("í", "i").replaceAll("ó", "o")
					  			  								 .replaceAll("ú", "u");
	
	                    if (m.getTitle().toLowerCase().contains(newText.toLowerCase())
	                    	|| m.getSnippet().toLowerCase().contains(newText.toLowerCase())
	                    	|| titleWithoutSpCh.toLowerCase().contains(newText.toLowerCase()) 
	                    	|| subtitleWithoutSpCh.toLowerCase().contains(newText.toLowerCase())){
	                    	queryMarkers.add(m);
	                    }
					}
					
					if(queryMarkers.size() != 0){
						for(int j = 0; j < queryMarkers.size(); j++){
							Marker queryMarker = queryMarkers.get(j);
							queryMarker.setVisible(true);
						}
						
						//Se pone la barra de búsqueda en su estado original.
						if(searchViewEditText != null){
							searchViewEditText.setError(null);
				        }
						
					}else{
						//Se cambia el estado de la barra de búsqueda, pues no se encontraron resultados.
						if(searchViewEditText != null){
							searchViewEditText.setError(getResources()
														.getString(R.string.no_results_found));
				        }
						
					}
				}else{
					//Se pone la barra de búsqueda en su estado original.
					if(searchViewEditText != null){
						searchViewEditText.setError(null);
			        }
					
					//Si no hay texto a buscar, todos los markers son visibles.
					for(int k = 0; k < fixedMarkersList.size(); k++){
						Marker marker = fixedMarkersList.get(k);
						marker.setVisible(true);
					}
				}
				
				if(queryMarkers.size() == 1){  //Quiere decir que hay un único marker visible.
					campusMap.animateCamera(CameraUpdateFactory
											.newLatLngZoom(queryMarkers.get(0).getPosition(), minZoom));
				}else{
					//Si no se encontraron marcadores asociados o no es único el resultado, se muestra
					//toda la Universidad.
					campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
				}
	     }
	 }
	 
	 public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
	    switch (itemSelected){
	    	case R.string.places:
	    		openSelectedItem = new Intent(MapHandler.this, Places.class); 
	    		break;
	    	case R.string.log_in:
		    	openSelectedItem = new Intent(MapHandler.this, MapAccess.class); 
		    	break;
		    case R.string.suggestions:
		    	openSelectedItem = new Intent(MapHandler.this, Suggestions.class); 
		    	break;
	        case R.string.about_us:
	        	openSelectedItem = new Intent(MapHandler.this, AboutUs.class); 
	        	break;
	        case R.string.log_out:
	        	Intent logOut = new Intent(MapHandler.this, MapHandler.class);
				Bundle actionCode = new Bundle();
				actionCode.putInt("actionCode", CLEAR_USER_DATA);
				logOut.putExtras(actionCode);
				logOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(logOut);
				finish();
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
	    	case R.id.places:
	    		openSelectedItem = new Intent(MapHandler.this, Places.class); 
	    		break;
	    	case R.id.login:
		    	openSelectedItem = new Intent(MapHandler.this, MapAccess.class); 
		    	break;
		    case R.id.suggestions:
		    	openSelectedItem = new Intent(MapHandler.this, Suggestions.class); 
		    	break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(MapHandler.this, AboutUs.class); 
	        	break;
	        case R.id.logout:
	        	Intent logOut = new Intent(MapHandler.this, MapHandler.class);
				Bundle actionCode = new Bundle();
				actionCode.putInt("actionCode", CLEAR_USER_DATA);
				logOut.putExtras(actionCode);
				logOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(logOut);
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
			if (UserData.getToken() == null){  //El usuario no está loggeado.
				menu.add(0, R.id.places, Menu.FIRST+1, getResources().getString(R.string.places));
				menu.add(0, R.id.login, Menu.FIRST+2, getResources()
						 .getString(R.string.log_in));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
			}else{
				menu.add(0, R.id.places, Menu.FIRST+1, getResources().getString(R.string.places));
				menu.add(0, R.id.suggestions, Menu.FIRST+2, getResources()
						 .getString(R.string.suggestions));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
		    	menu.add(0, R.id.logout, Menu.FIRST+4, getResources().getString(R.string.log_out));
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
			/*
			menu.findItem(R.id.places).setVisible(true);
			menu.findItem(R.id.login).setVisible(true);
			menu.findItem(R.id.suggestions).setVisible(false);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(false);
			*/
		}else{
			menuToShow.add(getResources().getString(R.string.places));
			menuToShow.add(getResources().getString(R.string.suggestions));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShow.add(getResources().getString(R.string.log_out));
			menuToShowIds.add(R.string.places);
			menuToShowIds.add(R.string.suggestions);
			menuToShowIds.add(R.string.about_us);
			menuToShowIds.add(R.string.log_out);
			/*
			menu.findItem(R.id.places).setVisible(true);
			menu.findItem(R.id.login).setVisible(false);
			menu.findItem(R.id.suggestions).setVisible(true);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(true);
			*/
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
        
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			
			@Override
			public boolean onClose() {
				for(int k = 0; k < fixedMarkersList.size(); k++){
					Marker marker = fixedMarkersList.get(k);
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
			
			@SuppressLint("DefaultLocale")
			@Override
			public boolean onQueryTextChange(String newTextNoTrim) {
				String newText = newTextNoTrim.trim();
				for(int i = 0; i < fixedMarkersList.size(); i++){
					Marker marker = fixedMarkersList.get(i);
					marker.setVisible(false);
				}
				if (newText != null && newText.length() != 0) {
					queryMarkers = new ArrayList<Marker>();
					for (Marker m : fixedMarkersList) {
	                	String titleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
	                     	   					  			  .replaceAll("í", "i").replaceAll("ó", "o")
	                     	   					  			  .replaceAll("ú", "u");
	                	String subtitleWithoutSpCh = m.getTitle().replaceAll("á", "a").replaceAll("é", "e")
  					  			  								 .replaceAll("í", "i").replaceAll("ó", "o")
  					  			  								 .replaceAll("ú", "u");
	
	                    if (m.getTitle().toLowerCase().contains(newText.toLowerCase())
	                    	|| m.getSnippet().toLowerCase().contains(newText.toLowerCase())
	                    	|| titleWithoutSpCh.toLowerCase().contains(newText.toLowerCase()) 
	                    	|| subtitleWithoutSpCh.toLowerCase().contains(newText.toLowerCase())){
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
					for(int k = 0; k < fixedMarkersList.size(); k++){
						Marker marker = fixedMarkersList.get(k);
						marker.setVisible(true);
					}
					campusMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UniEafit, minZoom));
				}
				
				return false;
	        }
		});
        
		return true;
	}

}
