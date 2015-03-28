package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.google.android.gms.maps.model.LatLng;
import com.sicomeafit.campusmovil.Adapters;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.models.ListItem;
import com.sicomeafit.campusmovil.models.MapData;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;


public class UserMarkers extends ListActivity {

	private Adapters adapter;

	public static final String USER_MARKER_CATEGORY = "marcador usuario";

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
		setContentView(R.layout.activity_user_markers);
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		//Se pasa el contexto y los datos a la clase Adapters para que los organice para la lista.
		adapter = new Adapters(this, generateData());

		setListAdapter(adapter);
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

	public static ArrayList<ListItem> generateData(){
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();

        //Se hace esto para obtener los marcadores del usuario en orden alfabético, ya que
        //el TreeMap ordena los nombres a medida que se van insertando.
        //Básicamente es cambiar de ordenar por posición a ordenar por nombre del marcador.
        Map<String, LatLng> userMarkers = new TreeMap<String, LatLng>();

		for(Map.Entry<LatLng, String> userMarker : MapData.getUserMarkers().entrySet()){
			userMarkers.put(userMarker.getValue(), userMarker.getKey());
		}

        for(Map.Entry<String, LatLng> userMarker : userMarkers.entrySet()){
            listItems.add(new ListItem(userMarker.getKey(), "", USER_MARKER_CATEGORY,
                    userMarker.getValue()));
        }

		return listItems;
	}

	public void logout(){
		Intent logOut = new Intent(UserMarkers.this, MapHandler.class);
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
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		ListItem itemSelected = (ListItem) l.getItemAtPosition(position);
		String windowTitle = itemSelected.getTitle();
		if(windowTitle != getResources().getString(R.string.no_results_found)){
			LatLng windowPosition = itemSelected.getPosition();
			MapHandler.manageUserMarker(windowPosition, windowTitle, UserMarkers.this);
		}
	}

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.map:
			openSelectedItem = new Intent(UserMarkers.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(UserMarkers.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.suggestions:
			openSelectedItem = new Intent(UserMarkers.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(UserMarkers.this, AboutUs.class);
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
			openSelectedItem = new Intent(UserMarkers.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.places:
			openSelectedItem = new Intent(UserMarkers.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.suggestions:
			openSelectedItem = new Intent(UserMarkers.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.aboutUs:
			openSelectedItem = new Intent(UserMarkers.this, AboutUs.class);
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

	public void handleSearchWidget(){
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {

			@Override
			public boolean onClose() {
				adapter.getFilter().filter(null); //Se envía null para que se muestren nuevamente
				//todos los marcadores el usuario.
				return false;
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ListItem itemSelected = (ListItem) adapter.getItem(0);
				String windowTitle = itemSelected.getTitle();
				if(windowTitle != getResources().getString(R.string.no_results_found)){
					//Esto llevaría la app al primer lugar en la lista.
					LatLng windowPosition = itemSelected.getPosition();
					MapHandler.manageUserMarker(windowPosition, windowTitle, UserMarkers.this);
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
			menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
			menu.add(0, R.id.suggestions, Menu.FIRST+3, getResources().getString(R.string.suggestions));
			menu.add(0, R.id.aboutUs, Menu.FIRST+4, getResources().getString(R.string.about_us));
			menu.add(0, R.id.logout, Menu.FIRST+5, getResources().getString(R.string.log_out));
		}
		getMenuInflater().inflate(R.menu.user_markers, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el menú como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.

		menuToShow.add(getResources().getString(R.string.map));
		menuToShow.add(getResources().getString(R.string.places));
		menuToShow.add(getResources().getString(R.string.suggestions));
		menuToShow.add(getResources().getString(R.string.about_us));
		menuToShow.add(getResources().getString(R.string.log_out));
		menuToShowIds.add(R.string.map);
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
