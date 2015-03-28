package com.sicomeafit.campusmovil.controllers;

import com.sicomeafit.campusmovil.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import java.lang.reflect.Type;


public class MainActivity extends Activity {

    private TextView campusMovilLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        campusMovilLabel = (TextView) findViewById(R.id.campus_movil_label);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/BubblegumSans-Regular.otf");
        campusMovilLabel.setTypeface(customFont);
        Bundle paramsBag = getIntent().getExtras();  //	Aquí estarían los parámetros recibidos.
		if(paramsBag != null){  //Se cierra la app completa pues el usuario presionó "Exit".
			finish();
		}else{
			Thread splashTimer = new Thread(){
				public void run(){
					try{
						sleep(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}finally{
						Intent openMapAccess = new Intent(MainActivity.this, MapHandler.class);
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
