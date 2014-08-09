package com.sicomeafit.campusmovil;

import java.util.ArrayList;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;


public class Places extends ListActivity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_places);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Se pasa el contexto y los datos a la clase Adapters para que los organice para la lista.
        Adapters adapter = new Adapters(this, generateData());
       
        setListAdapter(adapter);
	}

	private ArrayList<ListItem> generateData(){
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
        
        String windowTitle = MapData.getMarkersTitles().get(position);
		String windowSubtitle = MapData.getMarkersSubtitles().get(position);
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
		    case R.id.suggestions:
	        	openSelectedItem = new Intent(Places.this, Suggestions.class); 
	        	break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(Places.this, AboutUs.class); 
	        	break;
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
			menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
			menu.add(0, R.id.suggestions, Menu.FIRST+2, getResources()
					 .getString(R.string.suggestions));
	    	menu.add(0, R.id.aboutUs, Menu.FIRST+3, getResources().getString(R.string.about_us));
		}
		getMenuInflater().inflate(R.menu.places, menu);
		return true;
	}
	
}
