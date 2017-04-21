package app.com.example.android.adoptame;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import app.com.example.android.adoptame.connection.ConnectionHandler;
import app.com.example.android.adoptame.data.PetContract;

public class PetDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{

    private static final int DETAIL_LOADER = 0;
    private ShareActionProvider mShareActionProvider;

    private Uri mPhotoUri;
    private String mPhotoPath;

    private String[] mPetDetails;
    private Button contactButton, editButton, deleteButton;

    private AlertDialog dialog;

    public static final String EMAIL_PROTECTORA = "rdiezb01@estudiantes.unileon.es";
    private static final String TLFN_PROTECTORA = "987244271";

    private Uri intentData = null;
    private final static String PET_URI = "PetUri";

    private static final String[] PET_COLUMNS = {
            PetContract.PetsEntry._ID,
            PetContract.PetsEntry.COLUMN_PICTURE,
            PetContract.PetsEntry.COLUMN_NAME,
            PetContract.PetsEntry.COLUMN_GENDER,
            PetContract.PetsEntry.COLUMN_SIZE,
            PetContract.PetsEntry.COLUMN_AGE,
            PetContract.PetsEntry.COLUMN_WEIGHT,
            PetContract.PetsEntry.COLUMN_SPECIES,
            PetContract.PetsEntry.COLUMN_BREED,
            PetContract.PetsEntry.COLUMN_DESCRIPTION
    };

    // projection changes
    public static final int COL_PET_ID = 0;
    public static final int COL_PET_PICTURE = 1;
    public static final int COL_PET_NAME = 2;
    public static final int COL_PET_GENDER = 3;
    public static final int COL_PET_SIZE = 4;
    public static final int COL_PET_AGE = 5;
    public static final int COL_PET_WEIGHT = 6;
    public static final int COL_PET_SPECIES = 7;
    public static final int COL_PET_BREED = 8;
    public static final int COL_PET_DESCRIPTION = 9;

    public PetDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_petdetail, container, false);

        contactButton = (Button)rootView.findViewById(R.id.contact_button);

        if (PetList.mUserEmail.equals(Login.ADMIN)) {
            editButton = (Button)rootView.findViewById(R.id.edit_button_petdetail);
            deleteButton = (Button)rootView.findViewById(R.id.delete_button_petdetail);

            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.admin_buttons_layout);
            ll.setVisibility(View.VISIBLE);
            contactButton.setVisibility(View.GONE);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        } else {
            contactButton.setOnClickListener(this);
        }

        return rootView;
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.contact_button:
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialoglayout = inflater.inflate(R.layout.dialog_select_contact, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Contactar mediante...");
                builder.setView(dialoglayout);
                dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                ImageView imgEmail = (ImageView) dialoglayout.findViewById(R.id.email_imageview);
                imgEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setType("image/*");
                        intent.setData(Uri.parse("mailto:" + EMAIL_PROTECTORA)); // only email apps should handle this
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Quiero adoptar");
                        intent.putExtra(Intent.EXTRA_TEXT, "Hola muy buenas, me gustaría adoptar esta mascota. Espero vuestra respuesta, saludos!\n\n" + prepareSharedDetails());

                        intent.putExtra(Intent.EXTRA_STREAM, mPhotoUri);

                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    }
                });

                ImageView imgPhone = (ImageView) dialoglayout.findViewById(R.id.phone_imageview);
                imgPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + TLFN_PROTECTORA));

                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                break;
            case R.id.edit_button_petdetail:
                Intent intent = new Intent(getActivity(), EditPet.class);
                intent.putExtra(Intent.EXTRA_TEXT, mPetDetails);
                intent.putExtra(Intent.EXTRA_REFERRER, mPhotoPath);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.delete_button_petdetail:
                Toast.makeText(getActivity().getApplicationContext(),"Borrando...", Toast.LENGTH_LONG).show();
                borraDatos(mPhotoPath, getActivity().getApplicationContext());
                break;
        }
    }

    public String prepareSharedDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("NOMBRE: " + mPetDetails[0] + "\n");
        sb.append("SEXO: " + mPetDetails[1] + "\n");
        sb.append("TAMAÑO: " + mPetDetails[2] + "\n");
        sb.append("EDAD: " + mPetDetails[3] + "\n");
        sb.append("PESO: " + mPetDetails[4] + "\n");
        sb.append("ESPECIE: " + mPetDetails[5] + "\n");
        sb.append("RAZA: " + mPetDetails[6] + "\n");
        sb.append("DESCRIPCIÓN: " + mPetDetails[7]);
        return sb.toString();
    }

    public void borraDatos(String photoUriSt, Context context) {
        String picture = photoUriSt.substring(photoUriSt.indexOf('-') + 1, photoUriSt.length());
        if (context == null) {
            context = getActivity().getApplicationContext();
        }
        deleteGalleryPicture(getActivity(), photoUriSt);
        new DeleteData(picture, context).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pet_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mPetDetails != null) {
            mShareActionProvider.setShareIntent(createShareDetailsIntent());
        }

    }

    public static void deleteGalleryPicture(Activity activity, String photoPath) {
        String[] projection = { MediaStore.Images.Media._ID };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { photoPath };

        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        } else {
            // File not found in media store DB
            Log.d("FOTO", "No se encontro la foto");
        }
        c.close();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }
        Uri data = intent.getData();
        if (data != null) {
            intentData = data;
        }

        return new CursorLoader(
                getActivity(),
                intentData,
                PET_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) { return; }

        mPhotoPath = ConnectionHandler.PICTURE_DIR + "/" + ConnectionHandler.PICTURE_PREFIX + data.getString(1);
        mPhotoUri = Uri.fromFile(new File(mPhotoPath));
        mPetDetails = buildPetDetails(data);
        showPetDetails();

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareDetailsIntent());
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private Intent createShareDetailsIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("image/*");

        shareIntent.putExtra(Intent.EXTRA_TEXT, prepareSharedDetails());
        shareIntent.putExtra(Intent.EXTRA_STREAM, mPhotoUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mascota quiere ser adoptada");

        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return shareIntent;
    }

    private void showPetDetails() {
        ImageView imageView = (ImageView) getView().findViewById(R.id.detail_fragment_picture);
        Glide.with(getActivity()).load(mPhotoUri).signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(imageView);

        ((TextView) getView().findViewById(R.id.detail_fragment_name_textview)).setText(mPetDetails[0]);
        ((TextView) getView().findViewById(R.id.detail_fragment_gender_textview)).setText(mPetDetails[1]);
        ((TextView) getView().findViewById(R.id.detail_fragment_size_textview)).setText(mPetDetails[2]);
        ((TextView) getView().findViewById(R.id.detail_fragment_age_textview)).setText(mPetDetails[3]);
        ((TextView) getView().findViewById(R.id.detail_fragment_weight_textview)).setText(mPetDetails[4]);
        ((TextView) getView().findViewById(R.id.detail_fragment_species_textview)).setText(mPetDetails[5]);
        ((TextView) getView().findViewById(R.id.detail_fragment_breed_textview)).setText(mPetDetails[6]);
        ((TextView) getView().findViewById(R.id.detail_fragment_description_textview)).setText(mPetDetails[7]);
    }

    private String[] buildPetDetails(Cursor data) {
        String[] details = new String[data.getColumnCount()];
        String aux;
        aux = data.getString(COL_PET_NAME);
        if (aux.equals("null")) {
            aux = "";
        }
        details[0] = aux;
        details[1] = data.getString(COL_PET_GENDER);
        details[2] = data.getString(COL_PET_SIZE);
        aux = data.getString(COL_PET_AGE);
        if (aux.equals("null")) {
            aux = "";
        }
        details[3] = aux;
        aux = data.getString(COL_PET_WEIGHT);
        if (aux.equals("null")) {
            aux = "";
        }
        details[4] = aux;
        details[5] = data.getString(COL_PET_SPECIES);
        aux = data.getString(COL_PET_BREED);
        if (aux.equals("null")) {
            aux = "";
        }
        details[6] = aux;
        aux = data.getString(COL_PET_DESCRIPTION);
        if (aux.equals("null")) {
            aux = "";
        }
        details[7] = aux;
        return details;
    }

    private class DeleteData extends AsyncTask<Void, Void, Void> {
        String picture;
        JSONObject JSONResponse;
        Context context;

        public DeleteData(String pic, Context c){
            picture = pic;
            context = c;
        }

        @Override
        protected Void doInBackground(Void... params) {

            ContentValues cv = new ContentValues();
            cv.put(PetContract.PetsEntry.COLUMN_PICTURE, picture);

            ConnectionHandler cn = new ConnectionHandler();
            JSONResponse = cn.postData(ConnectionHandler.ACTION_DELETE_PET, cv);

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
                        respuesta = "Error: No se han podido borrar los datos";
                    } else {
                        respuesta = "Los datos se han borrado";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(context, respuesta, Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}
