package app.com.example.android.adoptame;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import app.com.example.android.adoptame.connection.ConnectionHandler;
import app.com.example.android.adoptame.data.PetContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditPetFragment extends PetFormFragment {

    private String photoPath;
    private String photoSt;

    public EditPetFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        Intent intent = getActivity().getIntent();

        if (intent.hasExtra(Intent.EXTRA_TEXT) && intent.hasExtra(Intent.EXTRA_REFERRER)) {

            photoPath = intent.getStringExtra(Intent.EXTRA_REFERRER);
            photoSt = photoPath.substring(photoPath.indexOf('-') + 1);
            Uri uri = Uri.fromFile(new File(photoPath));
            Glide.with(getActivity()).load(uri).asBitmap().into(addPetImg);

            String[] fields = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
            String spinnerValue = "";
            int spinnerPosition = 0;

            nameEdit.setText(fields[0]);
            spinnerValue = fields[1];
            if (spinnerValue != null && !spinnerValue.equals("")) {
                spinnerPosition = sexAdapter.getPosition(spinnerValue);
                sexSpinner.setSelection(spinnerPosition);
            }
            spinnerValue = fields[2];
            if (spinnerValue != null && !spinnerValue.equals("")) {
                spinnerPosition = sizeAdapter.getPosition(spinnerValue);
                sizeSpinner.setSelection(spinnerPosition);
            }
            ageEdit.setText(fields[3]);
            weightEdit.setText(fields[4]);
            spinnerValue = fields[5];
            if (spinnerValue != null && !spinnerValue.equals("")) {
                spinnerPosition = speciesAdapter.getPosition(spinnerValue);
                speciesSpinner.setSelection(spinnerPosition);
            }
            breedEdit.setText(fields[6]);
            descriptionEdit.setText(fields[7]);

            sendButton.setText("Editar");
        }
        return rootView;
    }

    protected void enviaDatos() {
        Toast.makeText(getActivity().getApplicationContext(), "Editando...", Toast.LENGTH_LONG).show();
        if (imageChanged) {
            PetDetailFragment.deleteGalleryPicture(getActivity(), photoPath);
        }
        new UploadData(EDIT_CODE, photoSt).execute();
    }

}
