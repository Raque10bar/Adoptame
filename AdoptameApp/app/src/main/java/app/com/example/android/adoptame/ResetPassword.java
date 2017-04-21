package app.com.example.android.adoptame;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import app.com.example.android.adoptame.connection.ConnectionHandler;


public class ResetPassword extends AppCompatActivity {

    private EditText email;
    Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Restaurar contraseña");
        getSupportActionBar().show();

        setContentView(R.layout.activity_reset_password);

        email = (EditText)findViewById(R.id.email_reset_password_edittext);
        resetButton = (Button) findViewById(R.id.reset_password_button);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (email.getText().toString().equals("")) {
                    resetButton.setEnabled(false);
                } else {
                    resetButton.setEnabled(true);
                }
            }
        };

        email.addTextChangedListener(textWatcher);
    }

    public void resetPassword(View view) {
        Toast.makeText(getApplicationContext(), "Reseteando la contraseña...", Toast.LENGTH_SHORT).show();
        new AttemptResetPassword().execute();
    }

    class AttemptResetPassword extends AsyncTask<Void, Void, Void> {
        String emailSt;
        JSONObject JSONResponse;
        boolean stop = false;

        public AttemptResetPassword() {
            emailSt = email.getText().toString();
            if (!emailSt.equals("admin") && (!emailSt.contains("@") || !emailSt.contains("."))) {
                Toast.makeText(getApplicationContext(), R.string.email_error_toast, Toast.LENGTH_SHORT).show();
                stop = true;
            }
        }

        @Override
        protected Void doInBackground(Void... args) {
            if (stop) {
                return null;
            }

            ContentValues cv = new ContentValues();
            cv.put(ConnectionHandler.EMAIL_KEY, emailSt);
            if (emailSt.equals("admin")) {
                cv.put(ConnectionHandler.ADMIN_EMAIL_KEY, PetDetailFragment.EMAIL_PROTECTORA);
            }

            ConnectionHandler cn = new ConnectionHandler();
            JSONResponse = cn.postData(ConnectionHandler.ACTION_RESET_PASSWORD, cv);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (stop) {
                return;
            }

            String respuesta = "";
            if (JSONResponse == null) {
                respuesta = getResources().getString(R.string.connection_error);
            } else {
                try {
                    if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == ConnectionHandler.SUCCESS) {
                        respuesta = getResources().getString(R.string.reset_password_success);
                    } else if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == -1){
                        respuesta = getResources().getString(R.string.send_email_error);
                    } else {
                        respuesta = getResources().getString(R.string.reset_password_error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(getApplicationContext(), respuesta, Toast.LENGTH_LONG).show();
        }
    }
}
