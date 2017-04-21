package app.com.example.android.adoptame;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import app.com.example.android.adoptame.connection.ConnectionHandler;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password, repeatPassword;
    private Button register;
    private TextView login;
    private String emailSt, passwordSt, repeatPasswordSt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.register_class_name);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_register);
        getSupportActionBar().show();

        email = (EditText) findViewById(R.id.email_editText_register);
        password = (EditText) findViewById(R.id.password_editText_register);
        repeatPassword = (EditText) findViewById(R.id.repeat_password_editText_register);
        register = (Button) findViewById(R.id.register_button);
        login = (TextView) findViewById(R.id.login_textView);

        register.setOnClickListener(this);
        login.setOnClickListener(this);

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

        email.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);
        repeatPassword.addTextChangedListener(textWatcher);

        checkFieldsForEmptyValues();
    }

    private void checkFieldsForEmptyValues() {

        emailSt = email.getText().toString();
        passwordSt = password.getText().toString();
        repeatPasswordSt = repeatPassword.getText().toString();

        if (emailSt.equals("") || passwordSt.equals("") || repeatPasswordSt.equals("")) {
            register.setEnabled(false);
        } else {
            register.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:
                Toast.makeText(getApplicationContext(),"Registrando los datos...", Toast.LENGTH_SHORT).show();
                new AttemptRegister().execute();
                break;
            case R.id.login_textView:
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            default:
                break;
        }
    }

    class AttemptRegister extends AsyncTask<Void, Void, Void> {
        JSONObject JSONResponse;
        boolean stop = false;
        String respuesta = "";

        public AttemptRegister() {
            if (!passwordSt.equals(repeatPasswordSt)) {
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
            JSONResponse = cn.postData(ConnectionHandler.ACTION_REGISTER, cv);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (stop) {
                return;
            } else if (respuesta.equals("")){
                if (JSONResponse == null) {
                    respuesta = getResources().getString(R.string.connection_error);
                } else {
                    try {
                        if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == ConnectionHandler.SUCCESS) {
                            respuesta = getResources().getString(R.string.register_success);
                        } else {
                            if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == -1) {
                                respuesta = getResources().getString(R.string.register_error);
                            } else {
                                respuesta = "No se ha podido registrar. Vuelva a intentarlo más tarde";
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            Toast.makeText(getApplicationContext(),respuesta, Toast.LENGTH_LONG).show();
        }

    }

}
