package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.models.UserData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class InformationManager extends Activity {

	private String windowTitle;
	private String windowSubtitle;

	//Navigation Drawer (menú lateral).
	ListView drawer = null;
	DrawerLayout drawerLayout = null;
	ActionBarDrawerToggle toggle = null;
	ArrayList<String> menuToShow = new ArrayList<String>(); 
	ArrayList<Integer> menuToShowIds = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information_manager);
		setNavigationDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		setTitleAndSubtitle();
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

	public void setTitleAndSubtitle(){
		Bundle windowInformation = getIntent().getExtras();
		windowTitle = windowInformation.getString("windowTitle");
		windowSubtitle = windowInformation.getString("windowSubtitle");
		getActionBar().setTitle(windowTitle);
	}

	public void logout(){
		Intent logOut = new Intent(InformationManager.this, MapHandler.class);
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

	public void handleMenuEvents(int itemSelected){
		Intent openSelectedItem = null;
		switch (itemSelected){
		case R.string.map:
			openSelectedItem = new Intent(InformationManager.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.my_markers:
			openSelectedItem = new Intent(InformationManager.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.places:
			openSelectedItem = new Intent(InformationManager.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.log_in:
			openSelectedItem = new Intent(InformationManager.this, MapAccess.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.suggestions:
			openSelectedItem = new Intent(InformationManager.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.string.about_us:
			openSelectedItem = new Intent(InformationManager.this, AboutUs.class);
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
			openSelectedItem = new Intent(InformationManager.this, MapHandler.class); 
			openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.myMarkers:
			openSelectedItem = new Intent(InformationManager.this, UserMarkers.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.places:
			openSelectedItem = new Intent(InformationManager.this, Places.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.login:
			openSelectedItem = new Intent(InformationManager.this, MapAccess.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.suggestions:
			openSelectedItem = new Intent(InformationManager.this, Suggestions.class);
            openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			break;
		case R.id.aboutUs:
			openSelectedItem = new Intent(InformationManager.this, AboutUs.class);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()){
			if(UserData.getToken() == null){  //El usuario no se ha loggeado.
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
				menu.add(0, R.id.login, Menu.FIRST+3, getResources()
						.getString(R.string.log_in));
				menu.add(0, R.id.aboutUs, Menu.FIRST+4, getResources().getString(R.string.about_us));
			}else{
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.myMarkers, Menu.FIRST+2, getResources().getString(R.string.my_markers));
				menu.add(0, R.id.places, Menu.FIRST+3, getResources().getString(R.string.places));
				menu.add(0, R.id.suggestions, Menu.FIRST+4, getResources().getString(R.string.suggestions));
				menu.add(0, R.id.aboutUs, Menu.FIRST+5, getResources().getString(R.string.about_us));
				menu.add(0, R.id.logout, Menu.FIRST+6, getResources().getString(R.string.log_out));
			}
		}
		getMenuInflater().inflate(R.menu.information_manager, menu);
		menu.findItem(R.id.action_guide).setVisible(false);  //Se esconde debido a que se va
		//a mostrar el menú como un
		//Navigation Drawer.
		menuToShow.clear();       //Se borra el contenido del menú para setearlo correctamente.
		menuToShowIds.clear();	  //También sus ids.

		if (UserData.getToken() == null){  //El usuario no está loggeado.
			menuToShow.add(getResources().getString(R.string.map));
			menuToShow.add(getResources().getString(R.string.places));
			menuToShow.add(getResources().getString(R.string.log_in));
			menuToShow.add(getResources().getString(R.string.about_us));
			menuToShowIds.add(R.string.map);
			menuToShowIds.add(R.string.places);
			menuToShowIds.add(R.string.log_in);
			menuToShowIds.add(R.string.about_us);
		}else{
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
		}
		//Se setea el menú de acuerdo al estado del usuario.
		drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				android.R.id.text1, menuToShow));
		return true;
	}

}
