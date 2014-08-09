package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import java.util.HashMap;


import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
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
	private String username;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_handler);
		setMapView();
		setGeneralListeners();
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
	    campusMap.getUiSettings().setZoomControlsEnabled(false);
	    campusMap.getUiSettings().setCompassEnabled(true);
		Bundle paramsBag = getIntent().getExtras();  //	Aquí estarían los parámetros recibidos.
		if(paramsBag != null){
			username = paramsBag.getString("username"); //Se captura y se muestra cuando aparezca el mapa.
		}
		if(isInternetConnectionAvailable()){
			wantedService = "Add main markers";
			POST_URL = "http://campusmovilapp.herokuapp.com/api/v1/markers?auth=" + UserData.getToken();  
												   //Para dar autorización al acceso de los marcadores.;
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
    	Intent openBuildingInfo = new Intent(MapHandler.this, InformationManager.class); 					
    	Bundle windowInformation = new Bundle();                                                          
		windowInformation.putString("windowTitle", windowTitle);
		windowInformation.putString("windowSubtitle", windowSubtitle);
		openBuildingInfo.putExtras(windowInformation);
        startActivity(openBuildingInfo); 
	}
	
	@Override
	public void onMapClick(LatLng clickedPoint) {
		if(campusMapBounds.contains(new LatLng(clickedPoint.latitude, clickedPoint.longitude))){
		   //Es posible poner un marker en el punto clickeado.	
	    	campusMap.addMarker(new MarkerOptions().position(clickedPoint)
	    						.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker)));
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
				    if(responseJSON.size() != 0){
				    	ArrayList<String> markersTitles = new ArrayList<String>();
					    ArrayList<String> markersSubtitles = new ArrayList<String>();
					    ArrayList<String> markersCategories = new ArrayList<String>();
						for(int i = 0; i < responseJSON.size(); i++){
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
						Toast.makeText(getApplicationContext(), getResources()
									   .getString(R.string.welcome_msg) + " " + username + "!", 
									   Toast.LENGTH_LONG).show();
				    }else{
				    	progressDialog.dismiss();
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
								System.exit(0);
							}
						});
						
						AlertDialog connectionErrorDialog = builder.create();
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
	
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item){
		Intent openSelectedItem; 
	    switch (item.getItemId()){
	    	case R.id.places:
	    		openSelectedItem = new Intent(MapHandler.this, Places.class); 
	    	break;
		    case R.id.suggestions:
		    	openSelectedItem = new Intent(MapHandler.this, Suggestions.class); 
		    	break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(MapHandler.this, AboutUs.class); 
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
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			menu.add(0, R.id.places, Menu.FIRST+1, getResources().getString(R.string.places));
			menu.add(0, R.id.suggestions, Menu.FIRST+2, getResources()
					 .getString(R.string.suggestions));
	    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
		}
		getMenuInflater().inflate(R.menu.map_handler, menu);
		// Se agrega el SearchWidget.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.options_menu_main_search)
        		                                           .getActionView();

        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
		return true;
	}

}
