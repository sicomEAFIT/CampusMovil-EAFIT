package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import android.app.ListActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import com.sicomeafit.campusmovil.Adapters;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.models.ListItem;
import com.sicomeafit.campusmovil.models.MapData;

public class UserMarkersManager extends ListActivity {

	private Adapters adapter;
	private static final String NEW_NOTE_TITLE = "New note";
	private static final String NEW_NOTE_SUBTITLE = "Create a new note";
	private static final String NEW_NOTE_CATEGORY = "nueva nota";
	private static final String USER_MARKER_CATEGORY = "marcador usuario";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_markers_manager);
		Bundle paramsBag = getIntent().getExtras();
		getActionBar().setTitle(Html.fromHtml(
				getResources().getString(R.string.title_activity_user_markers_manager) + 
				" " + "<b><font color=\"" + getResources().getColor(R.color.light_blue) + 
				"\">" +paramsBag.getString("markerTitle") + "</font></b>")); 
		//Se pasa el contexto y los datos a la clase Adapters para que los organice para la lista.
		adapter = new Adapters(this, generateData());

		setListAdapter(adapter);
	}

	public static ArrayList<ListItem> generateData(){
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		//Primer item de la lista que ofrece la posibilidad de agregar una nueva nota.
		listItems.add(new ListItem(NEW_NOTE_TITLE, NEW_NOTE_SUBTITLE, NEW_NOTE_CATEGORY));
		for (int i = 0; i < MapData.getMarkersTitles().size(); i++){
			//TODO
			/*listItems.add(new ListItem(MapData.getUserMarkersTitles().get(i), 
					MapData.getUserMarkersSubtitles().get(i), USER_MARKER_CATEGORY));*/
		}

		return listItems;
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		//TODO
		super.onListItemClick(l, v, position, id);
		ListItem itemSelected = (ListItem) l.getItemAtPosition(position);
		String windowTitle = itemSelected.getTitle();
		String windowSubtitle = itemSelected.getSubtitle();
		if(windowTitle != getResources().getString(R.string.no_results_found)){
			//goToSelectedPlace(windowTitle, windowSubtitle);  
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_markers_manager, menu);
		return true;
	}

}
