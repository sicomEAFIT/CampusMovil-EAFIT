package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.SearchView;


public class Places extends ListActivity {

	private Adapters adapter;
	private static final int CLEAR_USER_DATA = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_places);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Se pasa el contexto y los datos a la clase Adapters para que los organice para la lista.
        adapter = new Adapters(this, generateData());
       
        setListAdapter(adapter);
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
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        ListItem itemSelected = (ListItem) l.getItemAtPosition(position);
        String windowTitle = itemSelected.getTitle();
		String windowSubtitle = itemSelected.getSubtitle();
    	Intent openBuildingInfo = new Intent(Places.this, InformationManager.class); 					
    	Bundle windowInformation = new Bundle();                                                          
		windowInformation.putString("windowTitle", windowTitle);
		windowInformation.putString("windowSubtitle", windowSubtitle);
		openBuildingInfo.putExtras(windowInformation);
        startActivity(openBuildingInfo);   
    }
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item){
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
		
		if (UserData.getToken() == null){  //El usuario no está loggeado.
			menu.findItem(R.id.map).setVisible(true);
			menu.findItem(R.id.login).setVisible(true);
			menu.findItem(R.id.suggestions).setVisible(false);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(false);
		}else{
			menu.findItem(R.id.map).setVisible(true);
			menu.findItem(R.id.login).setVisible(false);
			menu.findItem(R.id.suggestions).setVisible(true);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(true);
		}
		
		// Se agrega el SearchWidget.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.options_menu_main_search)
        		                                           .getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.getFilter().filter(newText);
				return false;
			}
		});
        
		return true;
	}
	
}
