package com.sicomeafit.campusmovil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;



public class InformationManager extends Activity {

	private String windowTitle;
	private String windowSubtitle;
	
	private static final int CLEAR_USER_DATA = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information_manager);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle windowInformation = getIntent().getExtras();
		windowTitle = windowInformation.getString("windowTitle");
		windowSubtitle = windowInformation.getString("windowSubtitle");
		setTitle(windowTitle);
	}
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item){
		Intent openSelectedItem; 
	    switch (item.getItemId()){
		    case R.id.map:
	    		openSelectedItem = new Intent(InformationManager.this, MapHandler.class); 
	    		openSelectedItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    		break;
	    	case R.id.places:
	    		openSelectedItem = new Intent(InformationManager.this, Places.class); 
	    		break;
	    	case R.id.login:
	    		openSelectedItem = new Intent(InformationManager.this, MapAccess.class); 
	    		break;
		    case R.id.suggestions:
		    	openSelectedItem = new Intent(InformationManager.this, Suggestions.class); 
		    	break;
	        case R.id.aboutUs:
	        	openSelectedItem = new Intent(InformationManager.this, AboutUs.class); 
	        	break;
	        case R.id.logout:
	        	Intent logOut = new Intent(InformationManager.this, MapHandler.class);
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
			if(UserData.getToken() == null){  //El usuario no se ha loggeado.
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
				menu.add(0, R.id.login, Menu.FIRST+3, getResources()
						 .getString(R.string.log_in));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+4, getResources().getString(R.string.about_us));
			}else{
				menu.add(0, R.id.map, Menu.FIRST+1, getResources().getString(R.string.map));
				menu.add(0, R.id.places, Menu.FIRST+2, getResources().getString(R.string.places));
				menu.add(0, R.id.suggestions, Menu.FIRST+3, getResources()
						 .getString(R.string.suggestions));
		    	menu.add(0, R.id.aboutUs, Menu.FIRST+4, getResources().getString(R.string.about_us));
		    	menu.add(0, R.id.logout, Menu.FIRST+5, getResources().getString(R.string.log_out));
			}
		}
		getMenuInflater().inflate(R.menu.information_manager, menu);
		
		if (UserData.getToken() == null){  //El usuario no está loggeado.
			menu.findItem(R.id.map).setVisible(true);
			menu.findItem(R.id.places).setVisible(true);
			menu.findItem(R.id.login).setVisible(true);
			menu.findItem(R.id.suggestions).setVisible(false);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(false);
		}else{
			menu.findItem(R.id.map).setVisible(true);
			menu.findItem(R.id.places).setVisible(true);
			menu.findItem(R.id.login).setVisible(false);
			menu.findItem(R.id.suggestions).setVisible(true);
			menu.findItem(R.id.aboutUs).setVisible(true);
			menu.findItem(R.id.logout).setVisible(true);
		}
        
		return true;
	}

}
