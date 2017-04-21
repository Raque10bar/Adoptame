package app.com.example.android.adoptame;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import app.com.example.android.adoptame.data.PetContract;

public class PetList extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private PetListFragment mFragment;
    public static String mUserEmail;
    private FloatingActionButton addPetButton;

    private ImageView cleanImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petlist);

        cleanImg = (ImageView) findViewById(R.id.clean_search_imageview);
        cleanImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clean();
            }

        });

        mFragment = (PetListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mUserEmail = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (mUserEmail.equals(Login.ADMIN)) {
            addPetButton = (FloatingActionButton) findViewById(R.id.add_pet_button);
            addPetButton.setVisibility(View.VISIBLE);
        }

    }


    public void openAddPet(View view) {
        Intent intent = new Intent(PetList.this, AddPet.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public void search (View view) {

        EditText name = (EditText)findViewById(R.id.search_name_edittext);

        CheckBox dogCheckbox = (CheckBox)findViewById(R.id.search_dog_checkbox);
        CheckBox catCheckbox = (CheckBox)findViewById(R.id.search_cat_checkbox);
        CheckBox othersCheckbox = (CheckBox)findViewById(R.id.search_others_checkbox);

        CheckBox maleCheckbox = (CheckBox)findViewById(R.id.search_male_checkbox);
        CheckBox femaleCheckbox = (CheckBox)findViewById(R.id.search_female_checkbox);

        CheckBox bigCheckbox = (CheckBox)findViewById(R.id.search_big_checkbox);
        CheckBox mediumCheckbox = (CheckBox)findViewById(R.id.search_medium_checkbox);
        CheckBox smallCheckbox = (CheckBox)findViewById(R.id.search_small_checkbox);


        Uri uri = null;

        String nameSt = name.getText().toString();
        if (!nameSt.isEmpty()) {
            uri = PetContract.PetsEntry.buildPetNameUri(nameSt, uri);
        }

        if (sendToUri(dogCheckbox, catCheckbox, othersCheckbox)) {
            if (dogCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSpeciesUri(dogCheckbox.getText().toString(), uri);
            }

            if (catCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSpeciesUri(catCheckbox.getText().toString(), uri);
            }

            if (othersCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSpeciesUri(othersCheckbox.getText().toString(), uri);
            }
        }

        if (!(maleCheckbox.isChecked() == femaleCheckbox.isChecked())) {
            if (maleCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSexUri(maleCheckbox.getText().toString(), uri);
            } else {
                uri = PetContract.PetsEntry.buildPetSexUri(femaleCheckbox.getText().toString(), uri);
            }
        }

        if (sendToUri(bigCheckbox, mediumCheckbox, smallCheckbox)) {
            if (bigCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSizeUri(bigCheckbox.getText().toString(), uri);
            }

            if (mediumCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSizeUri(mediumCheckbox.getText().toString(), uri);
            }

            if (smallCheckbox.isChecked()) {
                uri = PetContract.PetsEntry.buildPetSizeUri(smallCheckbox.getText().toString(), uri);
            }
        }

        Bundle bundle = null;
        if (uri != null) {
            bundle = new Bundle();
            bundle.putString("uri", uri.toString());
        }

        mFragment.changeLoader(bundle);

        mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }


    public boolean sendToUri (CheckBox cb1, CheckBox cb2, CheckBox cb3) {
        if ((cb1.isChecked() && cb2.isChecked() && cb3.isChecked()) || !(cb1.isChecked() || cb2.isChecked() || cb3.isChecked()) ) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_petlist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.action_search:
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                }
                break;
            case R.id.action_change_password:
                intent = new Intent(PetList.this, ChangePassword.class);
                intent.putExtra(Intent.EXTRA_TEXT, mUserEmail);
                startActivity(intent);
                break;
            case R.id.action_log_out:
                intent = new Intent(this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clean () {
        LinearLayout searchLayout = (LinearLayout) findViewById(R.id.search_linear_layout);
        EditText nameEditText = (EditText) searchLayout.getChildAt(0);
        nameEditText.setText("");

        View v = null;
        CheckBox cb = null;
        for (int i=1; i<searchLayout.getChildCount(); i++) {
            v = searchLayout.getChildAt(i);
            if (v instanceof CheckBox) {
                cb = (CheckBox) v;
                cb.setChecked(false);
            }
        }
    }

}

