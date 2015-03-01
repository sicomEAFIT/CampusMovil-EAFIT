package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.helpers.HttpHandler;
import com.sicomeafit.campusmovil.models.MapData;
import com.sicomeafit.campusmovil.models.Note;
import com.sicomeafit.campusmovil.models.UserData;


public class UserNotes extends Activity implements SubscribedActivities {

    private EditText etTitle;
    private EditText etNote;
    private TimePicker timePicker;
    private TableRow daysRow;
    boolean timeChanged = false;

    //Se utiliza para saber qué días fueron seleccionados por el usuario.
    private Map<String, Boolean> daysOfTheWeek = new HashMap<String, Boolean>() {{
        put("Mo", false);
        put("Tu", false);
        put("We", false);
        put("Th", false);
        put("Fr", false);
        put("Sa", false);
        put("Su", false);
    }};
    //Se utiliza para adicionar los TextViews a la TableRow.
    private String[] daysOfTheWeekStrings = new String[] {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};

    //Datos provenientes de la nota seleccionada.
    private String latitude;
    private String longitude;
    private String title;
    private String noteTitle;
    private boolean isNoteToModify = false;

    //Datos para presentar una nota ya existente.
    private String exTitle;
    private String exNote;
    private int exHour;
    private int exMinute;
    private String exDays;

    private HttpHandler httpHandler = new HttpHandler();
    private final String ACTION_NOTE_DATA = "/open_note"; // TODO nombre de la action
    private final String ACTION_SAVE_NOTE = "/save_note"; // TODO nombre de la action
    private Map<String, String> paramsForHttpPost = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notes);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        httpHandler.addListeningActivity(this);
        setActivityAndGetParams();
    }

    public void setActivityAndGetParams() {
        etTitle = (EditText) findViewById(R.id.enter_title);
        etNote = (EditText) findViewById(R.id.write_note);
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        daysRow = (TableRow) findViewById(R.id.days_row);
        setTimeListener();
        addViewsToDaysRow();

        Bundle paramsBag = getIntent().getExtras();
        latitude = String.valueOf(paramsBag.getDouble("markerLat"));
        longitude = String.valueOf(paramsBag.getDouble("markerLong"));
        title = paramsBag.getString("markerTitle");
        noteTitle = paramsBag.getString("noteTitle");

        if (!noteTitle.equals(UserMarkersManager.NEW_NOTE_TITLE)) {
            //El usuario pretende ver/modificar una nota ya creada.
            //Por eso, se debe hacer una petici�n al server de los datos de la nota seleccionada.
            isNoteToModify = true;
            setNoteData();
        }
    }

    public void setTimeListener() {
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeChanged = true;
            }
        });
    }

    public void addViewsToDaysRow() {
        for (int i = 0; i < daysOfTheWeekStrings.length; ++i) {
            TextView day = new TextView(this);
            day.setText(daysOfTheWeekStrings[i]);
            day.setTextSize(TypedValue.COMPLEX_UNIT_SP, 27);
            day.setGravity(Gravity.CENTER_HORIZONTAL);
            day.setPadding(15, 15, 15, 15);
            day.setBackground(getResources().getDrawable(R.drawable.days_row_shape));
            day.setTextColor(Color.BLACK);
            day.setClickable(true);
            day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectOrRemoveDay(v);
                }
            });

            daysRow.addView(day);
        }
    }

    public void selectOrRemoveDay(View v) {
        TextView clickedDay = (TextView) v;
        boolean dayFound = false;
        for (String keyDay : daysOfTheWeek.keySet()) {
            if (!dayFound && clickedDay.getText().toString().equals(keyDay)) {
                if (daysOfTheWeek.get(keyDay)) {
                    v.setBackground(getResources().getDrawable(R.drawable.days_row_shape));
                    daysOfTheWeek.put(keyDay, false);
                } else {
                    //Se cambia el fondo del TextView para simbolizar su selección.
                    v.setBackground(getResources().getDrawable(R.drawable.days_row_clicked_shape));
                    daysOfTheWeek.put(keyDay, true);
                }
                dayFound = true;
            }
        }
    }

    public void setNoteData() {
        Note note = MapData.getUserNotes().get(noteTitle);

        exTitle = note.getTitle();
        exNote = note.getContent();
        exHour = note.getHour();
        exMinute = note.getMinute();
        exDays = note.getDays();

        etTitle.setText(exTitle);
        etNote.setText(exNote);
        if (exHour != -1 && exMinute != -1) {
            timePicker.setCurrentHour(exHour);
            timePicker.setCurrentMinute(exMinute);
        }
        if (!exDays.isEmpty()) {
            String[] splitDays = exDays.split(",");
            for (int i = 0; i < daysOfTheWeekStrings.length; ++i) {
                TextView d = (TextView) daysRow.getChildAt(i);
                for (int j = 0; j < splitDays.length; ++j) {
                    if (d.getText().toString().equals(splitDays[j])) {
                        daysRow.getChildAt(i).setBackground(getResources()
                                .getDrawable(R.drawable.days_row_clicked_shape));
                        daysOfTheWeek.put(splitDays[j], true);
                    }
                }
            }
        }
    }

    public void saveNote(View v) {
        if (!etTitle.getText().toString().trim().isEmpty()) {
            if (!etNote.getText().toString().trim().isEmpty()) {
                //En este punto se sabe que los campos fueron llenados satisfactoriamente.
                int hour = -1;
                int minute = -1;

                if (timeChanged) {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                }

                String selectedDays = "";
                for (int i = 0; i < daysOfTheWeekStrings.length; ++i) {
                    if (daysOfTheWeek.get(daysOfTheWeekStrings[i])) {
                        selectedDays += daysOfTheWeekStrings[i] + ",";
                    }
                }
                if (!selectedDays.isEmpty()) {
                    //Se hace para eliminar la última coma del String.
                    selectedDays = selectedDays.substring(0, selectedDays.length());
                }

                saveCurrentNote(etTitle.getText().toString().trim(),
                        etNote.getText().toString().trim(), hour, minute, selectedDays);

            } else {
                etNote.setError(getResources().getString(R.string.fill_this_field));
            }
        } else {
            etTitle.setError(getResources().getString(R.string.fill_this_field));
        }
    }

    public void saveCurrentNote(String noteTitle, String note, int hour, int minute, String days) {
        if (httpHandler.isInternetConnectionAvailable(this)) {
            paramsForHttpPost.put("latitude", latitude);
            paramsForHttpPost.put("longitude", longitude);
            paramsForHttpPost.put("markerTitle", title);
            paramsForHttpPost.put("title", noteTitle);
            paramsForHttpPost.put("note", note);
            paramsForHttpPost.put("hour", String.valueOf(hour));
            paramsForHttpPost.put("minute", String.valueOf(minute));
            paramsForHttpPost.put("days", days);
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_SAVE_NOTE,
                    "?auth=" + UserData.getToken(), paramsForHttpPost,
                    new HttpPost(), UserNotes.this);
        } else {
            Toast.makeText(getApplicationContext(), getResources()
                            .getString(R.string.internet_connection_required),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View v) {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_notes, menu);
        return true;
    }

    @Override
    public void notify(String action, ArrayList<JSONObject> responseJson) {
        // TODO Auto-generated method stub

    }
}
