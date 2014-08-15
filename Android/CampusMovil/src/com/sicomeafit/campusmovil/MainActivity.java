package com.sicomeafit.campusmovil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;


public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bundle paramsBag = getIntent().getExtras();  //	Aquí estarían los parámetros recibidos.
		if(paramsBag != null){  //Se cierra la app completa pues el usuario presionó "Exit".
			finish();
		}else{
			Thread splashTimer = new Thread(){
		        public void run(){
		          try{
		            sleep(2000);
		          }catch(InterruptedException e){
		            e.printStackTrace();
		          }finally{
		        	Intent openMapAccess = new Intent(MainActivity.this, MapAccess.class);
		      		startActivity(openMapAccess);
		      		finish();                           
		          }
		        }
		      };
		      
		      splashTimer.start();
		}	      
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
