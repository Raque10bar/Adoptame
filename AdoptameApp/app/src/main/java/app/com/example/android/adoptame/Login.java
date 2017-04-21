package app.com.example.android.adoptame;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import app.com.example.android.adoptame.connection.ConnectionHandler;


public class Login extends AppCompatActivity implements View.OnClickListener{

    private EditText email, password;
    private Button login;
    private ToggleButton toggleButton;
    private TextView register;
    private TextView forgotten_password;
    private String emailSt, passwordSt;

    public static final String ADMIN = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.login_class_name);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_login);
        getSupportActionBar().show();

        email = (EditText)findViewById(R.id.email_editText);
        password = (EditText)findViewById(R.id.password_editText);
        login = (Button) findViewById(R.id.login_button);
        register = (TextView) findViewById(R.id.register_textView);
        forgotten_password = (TextView) findViewById(R.id.forgotten_password_textView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forgotten_password.setOnClickListener(this);

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

        checkFieldsForEmptyValues();

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(null);
                    password.setSelection(password.getText().length());
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    password.setSelection(password.getText().length());
                }
            }
        });

        password.setTransformationMethod(new PasswordTransformationMethod());

        final View activityRootView = findViewById(R.id.login_root_view);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        int heightView = activityRootView.getHeight();
                        int widthView = activityRootView.getWidth();

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)register.getLayoutParams();

                        if (1.0 * widthView / heightView > 1) {
                            forgotten_password.setVisibility(View.GONE);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            register.setLayoutParams(params);
                        } else {
                            forgotten_password.setVisibility(View.VISIBLE);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                        }
                    }
                }
        );
    }


    private void checkFieldsForEmptyValues(){

        emailSt = email.getText().toString();
        passwordSt = password.getText().toString();

        if(emailSt.equals("")|| passwordSt.equals("")){
            login.setEnabled(false);
        } else {
            login.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.login_button:
                new AttemptLogin().execute();
                break;
            case R.id.register_textView:
                intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                break;
            case R.id.forgotten_password_textView:
                intent = new Intent(Login.this, ResetPassword.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    class AttemptLogin extends AsyncTask <Void, Void, Void> {

        JSONObject JSONResponse;
        boolean stop = false;

        public AttemptLogin() {
            if (!emailSt.equals(ADMIN) && (!emailSt.contains("@") || !emailSt.contains("."))) {
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
            cv.put(ConnectionHandler.PASSWORD_KEY, passwordSt);

            ConnectionHandler cn = new ConnectionHandler();
            JSONResponse = cn.postData(ConnectionHandler.ACTION_LOGIN, cv);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (stop) {
                return;
            }

            String response = "";
            if (JSONResponse == null) {
                response = getResources().getString(R.string.connection_error);
            } else {
                try {
                    if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == ConnectionHandler.SUCCESS) {
                        Intent intent = new Intent(Login.this, PetList.class);
                        intent.putExtra(Intent.EXTRA_TEXT, emailSt);
                        startActivity(intent);
                        finish();
                        return;
                    } else if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == -1){
                        response = getResources().getString(R.string.login_email_error);
                    } else if(JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) == 0) {
                        response = getResources().getString(R.string.login_password_error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(getApplicationContext(),response, Toast.LENGTH_LONG).show();
        }

    }

}
