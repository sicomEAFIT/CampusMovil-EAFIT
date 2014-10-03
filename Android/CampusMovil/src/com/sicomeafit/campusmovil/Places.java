package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
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


public class Places extends ListActivity {

	private Adapters adapter;
	private static final int CLEAR_USER_DATA = -1;
	
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
		setContentView(R.layout.activity_places);
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
	
	public static ArrayList<ListItem> generateData(){
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		for (int i = 0; i < MapData.getMarkersTitles().size(); i++){
		    listItems.add(new ListItem(MapData.getMarkersTitles().get(i), 
		    						   MapData.getMarkersSubtitles().get(i),
		    						   MapData.getMarkersCategories().get(i)));
		}
 
        return listItems;
    }
 
	/*
	 * Se usó para intentar mostrar los sitios lisados con SimpleAdapter.
	public void listPlaces(){
		List<Map<String, String>> placesList = new ArrayList<Map<String, String>>();
		Map<String, String> place = new HashMap<String, String>();
		for (int i = 0; i < MapData.getMarkersTitles().size(); i++){
		    place.put("title", MapData.getMarkersTitles().get(i));
		    place.put("subtitle", MapData.getMarkersSubtitles().get(i));
		    placesList.add(place);
		    place.clear();
		}
		SimpleAdapter adapter = new SimpleAdapter(this, placesList,
        android.R.layout.simple_list_item_2
					R.layout.custom_item,
                                  new String[] {"title", "subtitle"},
                                  new int[] {android.R.id.text1,
                                           android.R.id.text2
							R.id.text_item, R.id.text_subitem});
		lista.setAdapter(adapter);

	}
	*/
	
	public void goToSelectedPlace(String windowTitle, String windowSubtitle){
		Intent openBuildingInfo = new Intent(Places.this, InformationManager.class); 					
     	Bundle windowInformation = new Bundle();                                                          
 		windowInformation.putString("windowTitle", windowTitle);
 		windowInformation.putString("windowSubtitle", windowSubtitle);
 		openBuildingInfo.putExtras(windowInformation);
        startActivity(openBuildingInfo);
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
 		String windowSubtitle = itemSelected.getSubtitle();
 		if(windowTitle != getResources().getString(R.string.no_results_found)){
	     	goToSelectedPlace(windowTitle, windowSubtitle);  
        }
        
    }
	
	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
	    switch (itemSelected){
		    case R.string.map:
	        	openSelectedItem = new Intent(Places.this, MapHandler.class); 
	        	openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        	break;
		    case R.string.log_in:
	        	openSelectedItem = new Intent(Places.this, MapAccess.class); 
	        	break;
		    case R.string.suggestions:
	        	openSelectedItem = new Intent(Places.this, Suggestions.class); 
	        	break;
	        case R.string.about_us:
	        	openSelectedItem = new Intent(Places.this, AboutUs.class); 
	        	break;
	        case R.string.log_out:
	        	Intent logOut = new Intent(Places.this, MapHandler.class);
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
		    case R.id.map:
	        	openSelectedItem = new Intent(Places.this, MapHandler.class); 
	        	openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        	break;
		    case R.id.login:
	        	openSelectedItem = new Intent(Places.this, MapAccess.class); 
	        	break;
		    case R.id.suggestions:
	        	openSelectedItem = new Intent(Places.this, Suggestions.class); 
	        	break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(Places.this, AboutUs.class); 
	        	break;
	        case R.id.logout:
	        	Intent logOut = new Intent(Places.this, MapHandler.class);
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
			if (UserData.getToken() == null){  //El usuario no está loggeado.
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.login, Menu.FIRST+2, getResources()
						 .getString(R.string.log_in));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
			}else{
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.suggestions, Menu.FIRST+2, getResources()
						 .getString(R.string.suggestions));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
		    	menu.add(0, R.id.logout, Menu.FIRST+4, getResources().getString(R.string.log_out));
			}
		}
		getMenuInflater().inflate(R.menu.places, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
															 //a mostrar el menú como un
															 //Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.
		
		if (UserData.getToken() == null){  //El usuario no está loggeado.
			menuToShow.add(getResources().getString(R.string.map));
			menuToShow.add(getResources().getString(R.string.log_in));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShowIds.add(R.string.map);
			menuToShowIds.add(R.string.log_in);
			menuToShowIds.add(R.string.about_us);
			/*
			menu.findItem(R.id.map).setVisible(true);
			menu.findItem(R.id.login).setVisible(true);
			menu.findItem(R.id.suggestions).setVisible(false);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(false);
			*/
		}else{
			menuToShow.add(getResources().getString(R.string.map));
			menuToShow.add(getResources().getString(R.string.suggestions));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShow.add(getResources().getString(R.string.log_out));
			menuToShowIds.add(R.string.map);
			menuToShowIds.add(R.string.suggestions);
			menuToShowIds.add(R.string.about_us);
			menuToShowIds.add(R.string.log_out);
			/*
			menu.findItem(R.id.map).setVisible(true);
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
        searchView = (SearchView) menu.findItem(R.id.options_menu_main_search)
        		                                           .getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			
			@Override
			public boolean onClose() {
				adapter.getFilter().filter(null); //Se envía null para que se muestren nuevamente
												  //todos los lugares.
				return false;
			}
		});
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				ListItem itemSelected = (ListItem) adapter.getItem(0);
		        String windowTitle = itemSelected.getTitle();
				String windowSubtitle = itemSelected.getSubtitle();
				if(windowTitle != getResources().getString(R.string.no_results_found)){
					//Esto llevaría la app al primer lugar en la lista.
					goToSelectedPlace(windowTitle, windowSubtitle);
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
        
		return true;
	}
	
}
