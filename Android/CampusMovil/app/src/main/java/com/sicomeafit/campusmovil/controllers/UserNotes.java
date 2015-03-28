package com.sicomeafit.campusmovil.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
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

import com.google.android.gms.maps.model.LatLng;
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
    private int markerId;
    private String noteTitle;

    //Servirá para saber si se debe utilizar POST o PUT para enviar la nota.
    //Es decir, si es una nota nueva o se trata de una modificación en una existente.
    private boolean isNoteToModify = false;

    private HttpHandler httpHandler = new HttpHandler();
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
        markerId = paramsBag.getInt("markerId");
        noteTitle = paramsBag.getString("noteTitle");

        if (!noteTitle.equals(UserMarkersManager.NEW_NOTE_TITLE)) {
            //El usuario pretende ver/modificar una nota ya creada.
            //Por eso, se deberá hacer una petici�n al server tipo PUT en caso de
            //querer modificar la nota seleccionada.
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
        Note note = UserData.getUserNotes().get(noteTitle);

        etTitle.setText(note.getTitle());
        etNote.setText(note.getContent());
        if (note.getHour() != -1 && note.getMinute() != -1) {
            timePicker.setCurrentHour(note.getHour());
            timePicker.setCurrentMinute(note.getMinute());
        }
        if (!note.getDays().isEmpty()) {
            String[] splitDays = note.getDays().split(",");
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
            paramsForHttpPost.put("title", noteTitle);
            paramsForHttpPost.put("note", note);
            paramsForHttpPost.put("hour", String.valueOf(hour));
            paramsForHttpPost.put("minute", String.valueOf(minute));
            paramsForHttpPost.put("days", days);
            //Se debe enviar el id del marcador para poderlo relacionar dentro de la DB.
            paramsForHttpPost.put("markerId", String.valueOf(markerId));
            if (!isNoteToModify) {
                httpHandler.sendRequest(HttpHandler.API_V1, ACTION_SAVE_NOTE,
                        "?auth=" + UserData.getToken(), paramsForHttpPost,
                        new HttpPost(), UserNotes.this);
            }
            else {
                //Cuando se intenta modificar una nota existente, lo único que cambia
                //es que la petición será PUT y no POST.
                httpHandler.sendRequest(HttpHandler.API_V1, ACTION_SAVE_NOTE,
                        "?auth=" + UserData.getToken(), paramsForHttpPost,
                        new HttpPut(), UserNotes.this);
            }
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
        try {
            Log.i("responseJson", responseJson.toString());
            if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.SUCCESS){
                if(responseJson.get(0).getBoolean("success")){
                    etTitle.setText("");
                    etNote.setText("");
                    Time tNow = new Time();
                    tNow.setToNow();
                    int cHour = tNow.hour;
                    int cMinute = tNow.minute;
                    timePicker.setCurrentHour(cHour);
                    timePicker.setCurrentMinute(cMinute);
                    if (!isNoteToModify){
                        Toast.makeText(getApplicationContext(), getResources()
                                .getString(R.string.note_created), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getResources()
                                .getString(R.string.note_updated), Toast.LENGTH_LONG).show();
                    }
                    Intent openUserMarkersManager = new Intent(UserNotes.this, UserMarkersManager.class);
                    //Esto le hace saber al UserMarkersManager si es necesario actualizar
                    //la lista de notas.
                    openUserMarkersManager.putExtra("noteJustCreated", "noteCreated");
                    openUserMarkersManager.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(openUserMarkersManager);
                    finish();
                }else{
                    //Se muestra un diálogo para reintentar o cancelar el envío de la nota.
                    setBuilder(HttpHandler.NOT_SUCCEEDED_STRING);
                }
            }else{
                if(responseJson.get(responseJson.size()-1).getInt("status") == HttpHandler.UNAUTHORIZED){
                    setBuilder(HttpHandler.UNAUTHORIZED_STRING);
                }else{
                    setBuilder(HttpHandler.SERVER_INTERNAL_ERROR_STRING);
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        paramsForHttpPost.clear();

    }

    //Este método no requiere recibir la acción dado que en esta actividad se ejecuta un único servicio.
    public void setBuilder(String status){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserNotes.this);
        switch(status){
            case HttpHandler.NOT_SUCCEEDED_STRING:
                builder.setTitle(getResources().getString(R.string.oops));
                builder.setMessage(getResources().getString(R.string.note_not_saved));

                builder.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se intenta nuevamente la conexión con el servicio realizando
                        //otra vez el proceso desde saveNote().
                        //El parámetro es nulo porque en este caso no es requerido.
                        saveNote(null);
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etTitle.setText("");
                        etNote.setText("");
                        Time tNow = new Time();
                        tNow.setToNow();
                        int cHour = tNow.hour;
                        int cMinute = tNow.minute;
                        timePicker.setCurrentHour(cHour);
                        timePicker.setCurrentMinute(cMinute);
                        Intent openMap = new Intent(UserNotes.this, MapHandler.class);
                        openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(openMap);
                    }
                });

                break;
            case HttpHandler.UNAUTHORIZED_STRING:
                builder.setTitle(getResources().getString(R.string.log_in));
                builder.setMessage(getResources().getString(R.string.login_needed_4));

                builder.setPositiveButton(getResources().getString(R.string.log_in), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se hace un startActivityForResult para que se borren
                        //los datos del usuario. Luego se pasa al Log in.
                        Intent clearUserData = new Intent(UserNotes.this, MapHandler.class);
                        Bundle actionCode = new Bundle();
                        actionCode.putInt("actionCode", MapHandler.CLEAR_USER_DATA);
                        actionCode.putBoolean("isActivityForResult", true);
                        clearUserData.putExtras(actionCode);
                        startActivityForResult(clearUserData, 1);
                        //El 1 indica que cuando la actividad finalice, retornará a
                        //onActivityResult de esta actividad.
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitApp();
                    }
                });

                break;
            case HttpHandler.SERVER_INTERNAL_ERROR_STRING:
                builder.setTitle(getResources().getString(R.string.connection_error_title));
                builder.setMessage(getResources().getString(R.string.connection_error));

                builder.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Se intenta nuevamente la conexión con el servicio realizando
                        //otra vez el proceso desde saveNote().
                        //El parámetro es nulo porque en este caso no es requerido.
                        saveNote(null);
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitApp();
                    }
                });

                break;
        }

        AlertDialog userNoteAlertDialog = builder.create();
        userNoteAlertDialog.show();
    }

    public void exitApp(){
        Intent exitApp = new Intent(UserNotes.this, MainActivity.class);
        Bundle userActionInfo = new Bundle();
        userActionInfo.putBoolean("exit", true);
        exitApp.putExtras(userActionInfo);
        exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exitApp);
        finish();
    }
}
