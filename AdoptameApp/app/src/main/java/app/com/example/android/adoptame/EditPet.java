package app.com.example.android.adoptame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class EditPet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);
        getSupportActionBar().setTitle("Editar mascota");
    }

}
