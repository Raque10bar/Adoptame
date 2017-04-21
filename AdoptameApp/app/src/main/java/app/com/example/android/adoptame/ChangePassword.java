package app.com.example.android.adoptame;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import app.com.example.android.adoptame.connection.ConnectionHandler;


public class ChangePassword extends AppCompatActivity {

    private TextView emailTextview;
    private EditText password, repeatPassword;
    private Button changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Cambio de contraseña");
        getSupportActionBar().show();

        emailTextview = (TextView)findViewById(R.id.change_password_textview);
        password = (EditText) findViewById(R.id.new_password_edittext);
        repeatPassword = (EditText) findViewById(R.id.repeat_new_password_edittext);
        changePassword = (Button) findViewById(R.id.change_password_button);

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            emailTextview.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFieldsForEmptyValues();
            }
        };

        password.addTextChangedListener(textWatcher);
        repeatPassword.addTextChangedListener(textWatcher);

        checkFieldsForEmptyValues();

    }

    private void checkFieldsForEmptyValues(){

        String s1 = password.getText().toString();
        String s2 = repeatPassword.getText().toString();

        if(s1.equals("")|| s2.equals("")){
            changePassword.setEnabled(false);
        } else {
            changePassword.setEnabled(true);
        }
    }

    public void changePassword(View view) {
        Toast.makeText(getApplicationContext(),"Cambiando la contraseña...", Toast.LENGTH_SHORT).show();
        new AttemptChangePassword().execute();
    }

    class AttemptChangePassword extends AsyncTask<Void, Void, Void> {
        String emailSt;
        String passwordSt, repeatPasswordSt;

        JSONObject JSONResponse;
        boolean stop = false;

        public AttemptChangePassword() {
            emailSt = emailTextview.getText().toString();
            passwordSt = password.getText().toString();
            repeatPasswordSt = repeatPassword.getText().toString();
            if (!passwordSt.equals(repeatPasswordSt)){
                Toast.makeText(getApplicationContext(), R.string.different_password_error_toast, Toast.LENGTH_SHORT).show();
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
            cv.put(ConnectionHandler.PASSWORD_KEY, passwordSt);

            ConnectionHandler cn = new ConnectionHandler();
            JSONResponse = cn.postData(ConnectionHandler.ACTION_CHANGE_PASSWORD, cv);
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
                        respuesta = getResources().getString(R.string.change_password_success);
                    } else {
                        respuesta = getResources().getString(R.string.change_password_error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(getApplicationContext(),respuesta, Toast.LENGTH_LONG).show();
        }
    }

}
