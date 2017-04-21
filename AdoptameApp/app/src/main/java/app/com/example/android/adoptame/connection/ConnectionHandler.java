package app.com.example.android.adoptame.connection;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Clase que se encarga de las conexiones
 */
public class ConnectionHandler {

    public static final String PICTURE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Adoptame";
    public static final String PICTURE_PREFIX = "Image-";

    public static final String SERVER_URL = "http://107.170.32.62/";
    private static final String LOGIN_URL = "android_login_api/Login.php";
    private static final String REGISTER_URL = "android_login_api/Register.php";

    private static final String CHANGE_PASSWORD_URL = "android_login_api/ChangePassword.php";
    private static final String RESET_PASSWORD_URL = "android_login_api/ResetPassword.php";
    private static final String GET_DATA_URL = "android_login_api/GetPets.php";
    private static final String ADD_PET_URL = "android_login_api/AddPet.php";
    private static final String EDIT_PET_URL = "android_login_api/EditPet.php";
    private static final String DELETE_PET_URL = "android_login_api/DeletePet.php";

    public static final String PICTURES_URL = "pictures/";

    public static final int ACTION_LOGIN = 0;
    public static final int ACTION_REGISTER = 1;
    public static final int ACTION_CHANGE_PASSWORD = 2;
    public static final int ACTION_RESET_PASSWORD = 3;
    public static final int ACTION_ADD_PET = 4;
    public static final int ACTION_EDIT_PET = 5;
    public static final int ACTION_DELETE_PET = 6;

    public static final String TAG_SUCCESS = "success";
    public static final int SUCCESS = 1;

    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    public static final String ADMIN_EMAIL_KEY = "admin_email";

    /**
     * Método que envía los datos
     * @param action tipo de operación que queremos hacer con los datos
     * @param cv conjunto de pares de valores clave-dato con los datos a enviar
     * @return JSON con el resultado del envío de los datos
     */
    public JSONObject postData(int action, ContentValues cv){

        String urlSt;
        switch (action) {
            case ACTION_LOGIN:
                urlSt = SERVER_URL + LOGIN_URL;
                break;
            case ACTION_REGISTER:
                urlSt = SERVER_URL + REGISTER_URL;
                break;
            case ACTION_CHANGE_PASSWORD:
                urlSt = SERVER_URL + CHANGE_PASSWORD_URL;
                break;
            case ACTION_RESET_PASSWORD:
                urlSt = SERVER_URL + RESET_PASSWORD_URL;
                break;
            case ACTION_ADD_PET:
                urlSt = SERVER_URL + ADD_PET_URL;
                break;
            case ACTION_EDIT_PET:
                urlSt = SERVER_URL + EDIT_PET_URL;
                break;
            case ACTION_DELETE_PET:
                urlSt = SERVER_URL + DELETE_PET_URL;
                break;
            default:
                return null;
        }

        JSONObject jo = null;
        InputStream is;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlSt);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            String query = buildQuery(cv);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            connection.connect();

            is = connection.getInputStream();
            if (is == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder str = new StringBuilder();
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                str.append(strLine + "\n");
            }

            if (str.length() == 0) {
                return null;
            }

            is.close();
            jo = new JSONObject(str.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jo;
    }

    /**
     * Método que consigue los datos de las mascotas del servidor
     * @return JSONArray con la lista de datos de las mascotas
     */
    public JSONArray getData() {
        JSONArray ja = null;
        InputStream is;
        String urlSt = SERVER_URL + GET_DATA_URL;
        try {
            URL url = new URL(urlSt);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder str = new StringBuilder();
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                str.append(strLine + "\n");
            }
            is.close();
            ja = new JSONArray(str.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ja;
    }

    /**
     * Método que convierte conjuntos de datos clave-valor que queremos guardar en el servidor en una
     * String que se pueda enviar mediante POST
     * @param cv datos que queremos enviar
     * @return String con los datos
     */
    private String buildQuery(ContentValues cv){
        Set<Map.Entry<String, Object>> set=cv.valueSet();
        Iterator itr = set.iterator();

        Uri.Builder builder = new Uri.Builder();

        while(itr.hasNext()) {
            Map.Entry me = (Map.Entry) itr.next();
            try {
                builder.appendQueryParameter(me.getKey().toString(), me.getValue().toString());
            } catch (Exception e) {
                builder.appendQueryParameter(me.getKey().toString(), "null");
            }
        }

        return builder.build().getEncodedQuery();

    }

}
