package app.com.example.android.adoptame.connection;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.StringSignature;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import app.com.example.android.adoptame.data.PetContract;

/**
 * Clase que descarga los datos de las mascotas
 */

public class FetchPetsTask extends AsyncTask<Void,Void,Void> {

    private final Context mContext;

    public FetchPetsTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void...params){

        ConnectionHandler cn = new ConnectionHandler();
        JSONArray petsArray = cn.getData();
        try {
            getPetsDataFromJson(petsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método que extrae del array JSON los datos de las mascotas y los inserta en la base de datos
     * local del móvil
     * @param petsJsonArray array con los datos de las mascotas
     * @throws JSONException
     */

    private void getPetsDataFromJson(JSONArray petsJsonArray) throws JSONException {

        if (petsJsonArray != null) {

            Vector<ContentValues> cVVector = new Vector<ContentValues>(petsJsonArray.length());
            String pictures = "(";
            try {
                for (int i = 0; i < petsJsonArray.length(); i++) {
                    String pictureName = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_PICTURE);

                    if (i != petsJsonArray.length() - 1) {
                        pictures += "'" + pictureName + "',";
                    } else {
                        pictures += "'" + pictureName + "'";
                    }

                    String picturePath = ConnectionHandler.SERVER_URL + ConnectionHandler.PICTURES_URL + pictureName;
                    String name = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_NAME);
                    String sex = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_GENDER);
                    String size = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_SIZE);
                    String age = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_AGE);
                    String weight = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_WEIGHT);
                    String species = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_SPECIES);
                    String breed = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_BREED);
                    String description = petsJsonArray.getJSONObject(i).getString(PetContract.PetsEntry.COLUMN_DESCRIPTION);

                    Bitmap bitmap = getImage(picturePath);
                    saveImage(bitmap, pictureName);

                    ContentValues petValues = new ContentValues();
                    petValues.put(PetContract.PetsEntry.COLUMN_PICTURE, pictureName);
                    petValues.put(PetContract.PetsEntry.COLUMN_NAME, name);
                    petValues.put(PetContract.PetsEntry.COLUMN_GENDER, sex);
                    petValues.put(PetContract.PetsEntry.COLUMN_SIZE, size);
                    petValues.put(PetContract.PetsEntry.COLUMN_AGE, age);
                    petValues.put(PetContract.PetsEntry.COLUMN_WEIGHT, weight);
                    petValues.put(PetContract.PetsEntry.COLUMN_SPECIES, species);
                    petValues.put(PetContract.PetsEntry.COLUMN_BREED, breed);
                    petValues.put(PetContract.PetsEntry.COLUMN_DESCRIPTION, description);

                    cVVector.add(petValues);

                }
                pictures += ")";

                mContext.getContentResolver().delete(PetContract.PetsEntry.CONTENT_URI, PetContract.PetsEntry.COLUMN_PICTURE + " NOT IN " + pictures, null);

                int inserted = 0;
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(PetContract.PetsEntry.CONTENT_URI, cvArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private Bitmap getImage(String path) {
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(mContext).
                    load(path).
                    asBitmap().
                    signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).
                    into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).
                    get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Método que guarda la foto de la mascota en la galería del móvil
     * @param finalBitmap el bitmap con la imagen
     * @param imageName el nombre de la imagen
     * @return
     */
    private String saveImage(Bitmap finalBitmap, String imageName) {

        File myDir = new File(ConnectionHandler.PICTURE_DIR);
        myDir.mkdir();

        String iname = ConnectionHandler.PICTURE_PREFIX + imageName;
        File file = new File(myDir, iname);
        String image_path = myDir.getAbsolutePath() + "/" + iname;

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(file); // With 'permFile' being the File object
        mediaScannerIntent.setData(fileContentUri);
        mContext.sendBroadcast(mediaScannerIntent);

        return image_path;
    }

}


