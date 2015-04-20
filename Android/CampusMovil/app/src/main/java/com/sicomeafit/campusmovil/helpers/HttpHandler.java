package com.sicomeafit.campusmovil.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.sicomeafit.campusmovil.R;
import com.sicomeafit.campusmovil.controllers.MapHandler;
import com.sicomeafit.campusmovil.controllers.SubscribedActivities;
import com.sicomeafit.campusmovil.controllers.UserMarkers;


public class HttpHandler {

	/*
     * C�digo de Apoyo: http://hmkcode.com/android-parsing-json-data/
	 * 					http://hmkcode.com/android-send-json-data-to-server/
	 */

    //C�digos HTTP.
    public static final int SUCCESS = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int SERVER_INTERNAL_ERROR = 500;

    //Strings asociados con c�digos HTTP.
    public static final String NOT_SUCCEEDED_STRING = "not_succeeded";
    public static final String UNAUTHORIZED_STRING = "unauthorized";
    public static final String SERVER_INTERNAL_ERROR_STRING = "server_internal_error";

    //Dominio del servidor.
    private static final String DOMAIN = "http://campusmovilapp.herokuapp.com";//"http://104.236.191.184/";
    public static final String API_V1 = "/api/v1";

    private SubscribedActivities listeningActivity;

    //Par�metros necesarios para cumplir con las peticiones al Servidor.
    private String nameSpace;
    private String action;
    private String queryParams;
    private Map<String, String> paramsForHttpPost = new HashMap<String, String>();
    private HttpRequest httpRequest;
    private Context context;

    //En este punto se determina a qu� Activity ser� enviado el resultado de la petici�n.
    public void addListeningActivity(SubscribedActivities activity) {
        this.listeningActivity = activity;
    }

    public ArrayList<JSONObject> getInformation() {

        HttpGet httpGet = null;
        HttpPost httpPost = null;
        HttpPut httpPut = null;
        String url = DOMAIN + nameSpace + action + queryParams;
        String responseText = "";
        HttpClient httpClient = new DefaultHttpClient();
        JSONObject json = createJSONObject(action, this.paramsForHttpPost);
        HttpResponse httpResponse = null;
        int responseStatusCode = SERVER_INTERNAL_ERROR;  //Por defecto ser� el c�digo de error del Server.
        InputStream inputStream = null;
        JSONArray responseJsonArray = null;
        ArrayList<JSONObject> responseJson = new ArrayList<JSONObject>();

        //Se prepara la petici�n con los par�metros en caso de ser necesarios.
        if (httpRequest instanceof HttpGet) {
            httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
        } else if (httpRequest instanceof HttpPost) {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            try {
                httpPost.setEntity(new StringEntity(json.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (httpRequest instanceof HttpPut) {
            httpPut = new HttpPut(url);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            try {
                httpPut.setEntity(new StringEntity(json.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //Se ejecuta la petici�n.
        try {
            if (httpRequest instanceof HttpGet) {
                httpResponse = httpClient.execute(httpGet);
            } else if (httpRequest instanceof HttpPost) {
                httpResponse = httpClient.execute(httpPost);
            } else if (httpRequest instanceof HttpPut) {
                Log.i("put", json.toString());
                httpResponse = httpClient.execute(httpPut);
            }
            responseStatusCode = httpResponse.getStatusLine().getStatusCode();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = httpResponse.getEntity().getContent();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Se convierte inputStream a String.
        if (inputStream != null) {
            try {
                responseText = convertInputStreamToString(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            responseText = "Connection did not work!";  //Esto no ser� parseado. S�lo indica que no hubo
            //respuesta exitosa del Server por alg�n motivo.
        }

        try {
            if (responseText.charAt(0) != '[') { //Breve conversi�n a JSONArray si es un s�lo JSONObject.
                responseText = "[" + responseText + "]";
            }

            responseJsonArray = new JSONArray(responseText);
            responseJson = getJSONArrayOfJSONObjects(responseJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject statusJson = null;
        try {
            if (responseStatusCode == SUCCESS) {
                statusJson = new JSONObject("{'status':" + SUCCESS + "}");
            } else if (responseStatusCode == UNAUTHORIZED) {
                statusJson = new JSONObject("{'status':" + UNAUTHORIZED + "}");
            } else { //Se generaliza como error del Servidor.
                statusJson = new JSONObject("{'status': " + SERVER_INTERNAL_ERROR + "}");
            }
            responseJson.add(statusJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return responseJson;
    }

    public JSONObject createJSONObject(String action, Map<String, String> paramsForHttpPost) {
        JSONObject json = new JSONObject();
        JSONObject internJson = new JSONObject();
        try {
            //Servicio a ejecutar debido al par�metro action. Incluye datos que se anexan al objeto
            //tipo JSON.
            switch (action) {
                case "/auth":
                    internJson.put("username", paramsForHttpPost.get("username"));
                    internJson.put("password", paramsForHttpPost.get("password"));
                    json.put("user", internJson);
                    break;
                case "/register":
                    internJson.put("email", paramsForHttpPost.get("email"));
                    internJson.put("username", paramsForHttpPost.get("regUsername"));
                    internJson.put("password", paramsForHttpPost.get("regPassword"));
                    json.put("user", internJson);
                    break;
                case "/comment":
                    internJson.put("message", convertToUTF8(paramsForHttpPost.get("suggestion")));
                    json.put("comment", internJson);
                    break;
                case "/create_user_marker":
                    internJson.put("latitude", Double.parseDouble(paramsForHttpPost.get("latitude")));
                    internJson.put("longitude", Double.parseDouble(paramsForHttpPost.get("longitude")));
                    internJson.put("title", convertToUTF8(paramsForHttpPost.get("title")));
                    internJson.put("subtitle", null);
                    internJson.put("category", UserMarkers.USER_MARKER_CATEGORY);
                    json.put("marker", internJson);
                    break;
                case "/save_note":
                    internJson.put("title", convertToUTF8(paramsForHttpPost.get("title")));
                    internJson.put("content", convertToUTF8(paramsForHttpPost.get("note")));
                    internJson.put("hour", Integer.parseInt(paramsForHttpPost.get("hour")));
                    internJson.put("minute", Integer.parseInt(paramsForHttpPost.get("minute")));
                    internJson.put("days", paramsForHttpPost.get("days"));
                    internJson.put("marker_id", Integer.parseInt(paramsForHttpPost
                                                                .get("markerId")));
                    json.put("note", internJson);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();

        return result;
    }

    private ArrayList<JSONObject> getJSONArrayOfJSONObjects(JSONArray responseJsonArray) {
        ArrayList<JSONObject> responseJson = new ArrayList<JSONObject>();
        for (int i = 0; i < responseJsonArray.length(); i++) {
            try {
                JSONObject JsonObject = new JSONObject(responseJsonArray.get(i).toString());
                responseJson.add(JsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return responseJson;
    }

    public boolean isInternetConnectionAvailable(Context context) {
        boolean connectionFound = false;
        ConnectivityManager conMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            connectionFound = true;
        }
        return connectionFound;
    }

    //Convierte String a codificaci�n UTF-8.
    public static String convertToUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    public void sendRequest(String nameSpace, String action, String queryParams,
                            Map<String, String> paramsForHttpPostReceived, HttpRequest httpRequest, Context context) {
        this.nameSpace = nameSpace;
        this.action = action;
        this.queryParams = queryParams;
        this.paramsForHttpPost.clear();
        this.paramsForHttpPost.putAll(paramsForHttpPostReceived);
        this.httpRequest = httpRequest;
        this.context = context;
        new ServerConnection().execute();
    }

	/*
	 * Esta clase es necesaria para hacer la conexi�n asincr�nica con el Servidor.
	 * Se reciben par�metros para la ejecuci�n, tipo de par�metro de m�todo que indica progreso, 
	 * tipo de par�metro de que recibe el m�todo post-ejecuci�n.
	 */

    private class ServerConnection extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);

            switch (action) {
                case "/auth":
                    progressDialog.setMessage(context.getString(R.string.logging_in));
                    break;
                case "/register":
                    progressDialog.setMessage(context.getString(R.string.registering));
                    break;
                case "/markers":
                    progressDialog.setMessage(context.getString(R.string.loading_map_data));
                    break;
                case "/comment":
                    progressDialog.setMessage(context.getString(R.string.sending_suggestion));
                    break;
                case "/create_user_marker":
                    this.progressDialog.setMessage(context.getString(R.string.creating_your_marker));
                    break;
                case "/notes":
                    progressDialog.setMessage(context.getString(R.string.retrieving_marker_notes));
                    break;
                case "/save_note":
                    progressDialog.setMessage(context.getString(R.string.saving_note));
                    break;
            }
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        protected ArrayList<JSONObject> doInBackground(Void... params) {
            ArrayList<JSONObject> responseJson = getInformation();
            return responseJson;
        }

        protected void onPostExecute(ArrayList<JSONObject> responseJson) {
            progressDialog.dismiss();
            listeningActivity.notify(action, responseJson);
        }
    }

}
