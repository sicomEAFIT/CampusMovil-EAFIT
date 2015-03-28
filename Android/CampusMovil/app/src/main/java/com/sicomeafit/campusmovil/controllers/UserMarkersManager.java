package com.sicomeafit.campusmovil.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sicomeafit.campusmovil.Adapters;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.helpers.HttpHandler;
import com.sicomeafit.campusmovil.models.ListItem;
import com.sicomeafit.campusmovil.models.MapData;
import com.sicomeafit.campusmovil.models.Note;
import com.sicomeafit.campusmovil.models.UserData;


public class UserMarkersManager extends ListActivity implements SubscribedActivities {

	private Adapters adapter;
	public static ArrayList<ListItem> listItems = new ArrayList<ListItem>();
	private double latitude;
	private double longitude;
	private String title;

	//Strings necesarios para la lista de las notas asociadas a determinado marcador del usuario.
	public static final String NEW_NOTE_TITLE = "New note";
	private static final String NEW_NOTE_SUBTITLE = "Create a new note";
	private static final String NEW_NOTE_CATEGORY = "nueva nota";
	public static final String USER_MARKER_NOTE_CATEGORY = "nota usuario";

	private HttpHandler httpHandler = new HttpHandler();
	private final String ACTION_MARKER_NOTES = "/notes";
	private Map<String, String> paramsForHttpPost = new HashMap<String, String>();

	//Se declara el SearchView para utilizarlo luego y poder setear un String cuando se hace búsqueda
	//por voz.
	SearchView searchView = null;

	//Navigation Drawer (menú lateral).
	ListView drawer = null;
	DrawerLayout drawerLayout = null;
	ActionBarDrawerToggle toggle = null;
	ArrayList<String> menuToShow = new ArrayList<String>(); 
	ArrayList<Integer> menuToShowIds = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_markers_manager);
		setActivityAndGetParams();
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		httpHandler.addListeningActivity(this);

		retrieveUserMarkerNotes();
	}

	public void setActivityAndGetParams(){
		Bundle paramsBag = getIntent().getExtras();
		latitude = paramsBag.getDouble("markerLat");
		longitude = paramsBag.getDouble("markerLong");
		title = paramsBag.getString("markerTitle");
		getActionBar().setTitle(Html.fromHtml(
				getResources().getString(R.string.title_activity_user_markers_manager) + 
				" " + "<b><font color=\"" + getResources().getColor(R.color.light_blue) + 
				"\">" + title + "</font></b>")); 
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

	public void retrieveUserMarkerNotes(){
		//Trae las notas asociadas a ese marcador de usuario del servidor. 
		if(httpHandler.isInternetConnectionAvailable(this)){
            String queryParams = "?auth=" + UserData.getToken() + "&id=" +
                                 MapData.getUserMarkersIds().get(new LatLng(latitude, longitude));

            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_MARKER_NOTES, queryParams,
                                    paramsForHttpPost, new HttpGet(), UserMarkersManager.this);
		}else{
			Toast.makeText(getApplicationContext(), getResources()
					.getString(R.string.internet_connection_required), Toast.LENGTH_SHORT).show();
		}
	}

    public static ArrayList<ListItem> generateData(){
        ArrayList<ListItem> listItems = new ArrayList<ListItem>();
        listItems.add(new ListItem(NEW_NOTE_TITLE, NEW_NOTE_SUBTITLE, NEW_NOTE_CATEGORY, null));
        for(Map.Entry<String, Note> userMarkerNote : UserData.getUserNotes().entrySet()){
            listItems.add(new ListItem(userMarkerNote.getKey(), "", USER_MARKER_NOTE_CATEGORY,
                                        null));
        }

        return listItems;
    }

	public void goToSelectedNote(String noteTitle){
		Intent openNote = new Intent(UserMarkersManager.this, UserNotes.class);
        openNote.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		Bundle noteInformation = new Bundle(); 
		noteInformation.putInt("markerId", MapData.getUserMarkersIds()
                                                       .get(new LatLng(latitude, longitude)));
		noteInformation.putString("noteTitle", noteTitle);
		openNote.putExtras(noteInformation);
		startActivity(openNote);
	}

	public void logout(){
		Intent logOut = new Intent(UserMarkersManager.this, MapHandler.class);
		Bundle actionCode = new Bundle();
		actionCode.putInt("actionCode", MapHandler.CLEAR_USER_DATA);
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
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			String queryFirstUpper = query.substring(0, 1).toUpperCase() + query.substring(1);
			searchView.setQuery(queryFirstUpper, false);
			String newText = query.trim();
			adapter.getFilter().filter(newText);
			InputMethodManager inputMethodManager = (InputMethodManager) 
					getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
        if (intent.getExtras().getString("noteJustCreated") != null) {
            //Se hace actualización de la lista pues se acaba de crear una nueva nota.
            retrieveUserMarkerNotes();
        }
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		ListItem itemSelected = (ListItem) l.getItemAtPosition(position);
		String noteTitle = itemSelected.getTitle();
		if(noteTitle != getResources().getString(R.string.no_results_found)){
			goToSelectedNote(noteTitle);  
		}
	}

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.map:
			openSelectedItem = new Intent(UserMarkersManager.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.my_markers:
			openSelectedItem = new Intent(UserMarkersManager.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(UserMarkersManager.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.suggestions:
			openSelectedItem = new Intent(UserMarkersManager.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(UserMarkersManager.this, AboutUs.class);
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
		case R.id.map:
			openSelectedItem = new Intent(UserMarkersManager.this, MapHandler.class);
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.myMarkers:
			openSelectedItem = new Intent(UserMarkersManager.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.places:
			openSelectedItem = new Intent(UserMarkersManager.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.suggestions:
			openSelectedItem = new Intent(UserMarkersManager.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.aboutUs:
			openSelectedItem = new Intent(UserMarkersManager.this, AboutUs.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
		if(action.equals(ACTION_MARKER_NOTES)){
			try {
				Log.i("responseJson", responseJson.toString());
				if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
					listItems.clear();
                    //Se hace para tener en memoria sólo aquellas posibles notas a abrir.
                    UserData.clearUserNotes();
					//Primer item de la lista que ofrece la posibilidad de agregar una nueva nota.
					listItems.add(new ListItem(NEW_NOTE_TITLE, NEW_NOTE_SUBTITLE, NEW_NOTE_CATEGORY, null));
					for(int i = 0; i < responseJson.size()-1; i++){
						String title = responseJson.get(i).getString("title");
                        String content = responseJson.get(i).getString("content");
                        int hour = responseJson.get(i).getInt("hour");
                        int minute = responseJson.get(i).getInt("minute");
                        String days = responseJson.get(i).getString("days");
						listItems.add(new ListItem(title, "", USER_MARKER_NOTE_CATEGORY, null));
                        UserData.addUserNote(title, new Note(title, content, hour, minute, days));
					}

					//Cuando se tienen las notas asociadas al presente marcador de usuario, entonces
					//se procede a mostrar una lista que los contenga.
					adapter = new Adapters(this, listItems);
					setListAdapter(adapter);
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

	public void setBuilder(String status, final String action){
		AlertDialog.Builder builder = new AlertDialog.Builder(UserMarkersManager.this);
		switch(status){
		case HttpHandler.UNAUTHORIZED_STRING:
			builder.setTitle(getResources().getString(R.string.log_in));
			builder.setMessage(getResources().getString(R.string.login_needed_3));

			builder.setPositiveButton(getResources().getString(R.string.log_in), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Se hace un startActivityForResult para que se borren
					//los datos del usuario. Luego se pasa al Log in.
					Intent clearUserData = new Intent(UserMarkersManager.this, MapHandler.class);
					Bundle actionCode = new Bundle();
					actionCode.putInt("actionCode", MapHandler.CLEAR_USER_DATA);
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
					//Se intenta nuevamente la conexión con el servicio realizando 
					//otra vez el proceso desde retrieveUserMarkerNotes(). 
					retrieveUserMarkerNotes();
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

		AlertDialog userMarkersManagerAlertDialog = builder.create();
		userMarkersManagerAlertDialog.show();
	}

	public void exitApp(){
		Intent exitApp = new Intent(UserMarkersManager.this, MainActivity.class);
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
				adapter.getFilter().filter(null); //Se envía null para que se muestren nuevamente
				//todas las notas.
				return false;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ListItem itemSelected = (ListItem) adapter.getItem(0);
				String noteTitle = itemSelected.getTitle();
				if(noteTitle != getResources().getString(R.string.no_results_found)){
					//Esto llevaría la app a la primera nota en la lista.
					goToSelectedNote(noteTitle);  
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newTextNoTrim) {
				String newText = newTextNoTrim.trim();
				adapter.getFilter().filter(newText);
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
			menu.add(0, R.id.myMarkers, Menu.FIRST+2, getResources().getString(R.string.my_markers));
			menu.add(0, R.id.places, Menu.FIRST+3, getResources().getString(R.string.places));
			menu.add(0, R.id.suggestions, Menu.FIRST+4, getResources().getString(R.string.suggestions));
			menu.add(0, R.id.aboutUs, Menu.FIRST+5, getResources().getString(R.string.about_us));
			menu.add(0, R.id.logout, Menu.FIRST+6, getResources().getString(R.string.log_out));
		}
		getMenuInflater().inflate(R.menu.user_markers_manager, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el menú como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.

		menuToShow.add(getResources().getString(R.string.map));
		menuToShow.add(getResources().getString(R.string.my_markers));
		menuToShow.add(getResources().getString(R.string.places));
		menuToShow.add(getResources().getString(R.string.suggestions));
		menuToShow.add(getResources().getString(R.string.about_us));
		menuToShow.add(getResources().getString(R.string.log_out));
		menuToShowIds.add(R.string.map);
		menuToShowIds.add(R.string.my_markers);
		menuToShowIds.add(R.string.places);
		menuToShowIds.add(R.string.suggestions);
		menuToShowIds.add(R.string.about_us);
		menuToShowIds.add(R.string.log_out);

		//Se setea el menú lateral.
		drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				android.R.id.text1, menuToShow));
		//Se agrega el SearchWidget.
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.options_menu_main_search).getActionView();

		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		handleSearchWidget();

		return true;
	}

}
