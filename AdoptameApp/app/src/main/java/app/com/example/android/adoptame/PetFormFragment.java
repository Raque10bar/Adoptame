package app.com.example.android.adoptame;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import app.com.example.android.adoptame.connection.ConnectionHandler;
import app.com.example.android.adoptame.data.PetContract;
import app.com.example.android.adoptame.data.PetContract.PetsEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class PetFormFragment extends Fragment implements View.OnClickListener {

    private static final int GALLERY_CODE = 0;
    private static final int CAMERA_CODE = 1;

    protected static final int ADD_CODE = 0;
    protected static final int EDIT_CODE = 1;

    protected ImageView addPetImg;
    protected EditText nameEdit;
    protected EditText ageEdit;
    protected EditText weightEdit;
    protected EditText breedEdit;
    protected EditText descriptionEdit;

    protected Spinner sexSpinner, sizeSpinner, speciesSpinner;
    protected ArrayAdapter<CharSequence> sexAdapter, sizeAdapter, speciesAdapter;

    protected boolean imageChanged;

    //private String photoUriSt = null;
    private AlertDialog dialog;
    protected Button sendButton;

    public PetFormFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_pet_form, container, false);

        imageChanged = false;

        sexSpinner =(Spinner) rootView.findViewById(R.id.add_pet_sex_spinner);
        sizeSpinner =(Spinner) rootView.findViewById(R.id.add_pet_size_spinner);
        speciesSpinner =(Spinner) rootView.findViewById(R.id.add_pet_species_spinner);

        sexAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sex_spinner, android.R.layout.simple_spinner_item);
        sizeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.size_spinner, android.R.layout.simple_spinner_item);
        speciesAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.species_spinner, android.R.layout.simple_spinner_item);

        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sexSpinner.setAdapter(sexAdapter);
        sizeSpinner.setAdapter(sizeAdapter);
        speciesSpinner.setAdapter(speciesAdapter);

        addPetImg = (ImageView) rootView.findViewById(R.id.add_pet_imageview);
        addPetImg.setOnClickListener(this);
        nameEdit = (EditText) rootView.findViewById(R.id.add_pet_name_edittext);
        ageEdit = (EditText) rootView.findViewById(R.id.add_pet_age_edittext);
        weightEdit = (EditText) rootView.findViewById(R.id.add_pet_weight_edittext);
        breedEdit = (EditText) rootView.findViewById(R.id.add_pet_breed_edittext);
        descriptionEdit = (EditText) rootView.findViewById(R.id.add_pet_description_edittext);

        sendButton = (Button) rootView.findViewById(R.id.add_pet_send_button);
        sendButton.setOnClickListener(this);

        return rootView;
    }

    protected abstract void enviaDatos();

    public void onClick(View view) {

        if (view.getId() == R.id.add_pet_imageview) {
            showDialog();
        } else if (view.getId() == R.id.add_pet_send_button) {
            enviaDatos();
            sendButton.setEnabled(false);
        }
    }

    private void showDialog() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialoglayout = inflater.inflate(R.layout.dialog_select_image, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Seleccionar la foto desde...");
        builder.setView(dialoglayout);
        dialog = builder.create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT - 10;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT/3;

        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        ImageView imgGaleria = (ImageView) dialoglayout.findViewById(R.id.gallery_imageview);
        imgGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            ImageView imgCamara = (ImageView) dialoglayout.findViewById(R.id.camera_imageview);
            imgCamara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(intent, CAMERA_CODE);
                    }
                }
            });

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Uri targetUri = data.getData();
                Glide.with(getActivity())
                        .load(targetUri)
                        .asBitmap() // Uri of the picture
                        .into(addPetImg);
                sendButton.setEnabled(true);
                imageChanged = true;
            }
        }
        dialog.dismiss();
    }

    protected class UploadData extends AsyncTask<Void, Void, Void> {
        Bitmap image;
        String nameSt, sexSt, sizeSt, ageSt, weightSt, speciesSt, breedSt, descriptionSt, photoName;
        JSONObject JSONResponse;
        int requestCode;

        public UploadData(int code, String photo){
            requestCode = code;
            photoName = photo;
            image = ((BitmapDrawable)addPetImg.getDrawable()).getBitmap();
            nameSt = nameEdit.getText().toString().trim();
            sexSt = sexSpinner.getSelectedItem().toString().trim();
            sizeSt = sizeSpinner.getSelectedItem().toString().trim();
            ageSt = ageEdit.getText().toString().trim();
            weightSt = weightEdit.getText().toString().trim();
            speciesSt = speciesSpinner.getSelectedItem().toString().trim();
            breedSt = breedEdit.getText().toString().trim();
            descriptionSt = descriptionEdit.getText().toString().trim();

        }

        @Override
        protected Void doInBackground(Void... params) {
            InputStream is = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            try {
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            }catch (IOException e){

            }

            ContentValues cv = new ContentValues();
            cv.put(PetsEntry.COLUMN_PICTURE, encodedImage);
            if (nameSt.isEmpty()) {
                cv.putNull(PetsEntry.COLUMN_NAME);
            } else {
                cv.put(PetsEntry.COLUMN_NAME, nameSt);
            }
            cv.put(PetsEntry.COLUMN_GENDER, sexSt);
            cv.put(PetsEntry.COLUMN_SIZE, sizeSt);
            if (ageSt.isEmpty()) {
                cv.putNull(PetsEntry.COLUMN_AGE);
            } else {
                cv.put(PetsEntry.COLUMN_AGE, ageSt);
            }
            if (weightSt.isEmpty()) {
                cv.putNull(PetsEntry.COLUMN_WEIGHT);
            } else {
                cv.put(PetsEntry.COLUMN_WEIGHT, weightSt);
            }
            cv.put(PetsEntry.COLUMN_SPECIES, speciesSt);
            if (breedSt.isEmpty()) {
                cv.putNull(PetsEntry.COLUMN_BREED);
            } else {
                cv.put(PetsEntry.COLUMN_BREED, breedSt);
            }
            if (descriptionSt.isEmpty()) {
                cv.putNull(PetsEntry.COLUMN_DESCRIPTION);
            } else {
                cv.put(PetsEntry.COLUMN_DESCRIPTION, descriptionSt);
            }

            if (requestCode == EDIT_CODE) {
                cv.put(PetsEntry.COLUMN_PICTURE + "Name", photoName);
            }

            ConnectionHandler cn = new ConnectionHandler();
            if (requestCode == ADD_CODE) {
                JSONResponse = cn.postData(ConnectionHandler.ACTION_ADD_PET, cv);
            } else {
                JSONResponse = cn.postData(ConnectionHandler.ACTION_EDIT_PET, cv);
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String respuesta = "";
            if (JSONResponse == null) {
                respuesta = getResources().getString(R.string.connection_error);
            } else {
                try {
                    if (JSONResponse.getInt(ConnectionHandler.TAG_SUCCESS) != ConnectionHandler.SUCCESS) {
                        if (requestCode == EDIT_CODE) {
                            respuesta = "Error: No se han podido editar los datos";
                        } else {
                            respuesta = "Error: No se han podido guardar los datos";
                        }
                    } else {
                        if(requestCode == EDIT_CODE) {
                            respuesta = "Los datos han sido editados";
                        } else {
                            respuesta = "Los datos se han guardado";
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            sendButton.setEnabled(true);
            Toast.makeText(getActivity().getApplicationContext(), respuesta, Toast.LENGTH_LONG).show();
            getActivity().finish();

        }
    }
}
