package app.com.example.android.adoptame.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase con los valores de la base de datos
 */
public class PetContract {

    public static final String CONTENT_AUTHORITY = "app.com.example.android.adoptame";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USERS = "users";
    public static final String PATH_PETS = "pets";
    public static final String PATH_SEARCH = "search";

    /**
     * Clase para la tabla de las mascotas y los métodos necesarios para crear querys para recuperar
     * los datos
     */
    public static final class PetsEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";
        public static final String COLUMN_PICTURE = "image";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_SPECIES = "species";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_DESCRIPTION = "description";

        //Localización base de cada tabla
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PETS).build();

        public static final Uri SEARCH_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;


        public static Uri buildPetIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPetNameUri(String name, Uri uri) {

            if (uri == null) {
                uri = SEARCH_URI;
            }
            return uri.buildUpon().appendQueryParameter(COLUMN_NAME, name).build();
        }

        public static Uri buildPetSpeciesUri(String species, Uri uri) {

            if (uri == null) {
                uri = SEARCH_URI;
            }
            return uri.buildUpon().appendQueryParameter(COLUMN_SPECIES, species).build();
        }

        public static Uri buildPetSexUri(String sex, Uri uri) {

            if (uri == null) {
                uri = SEARCH_URI;
            }
            return uri.buildUpon().appendQueryParameter(COLUMN_GENDER, sex).build();
        }

        public static Uri buildPetSizeUri(String size, Uri uri) {

            if (uri == null) {
                uri = SEARCH_URI;
            }
            return uri.buildUpon().appendQueryParameter(COLUMN_SIZE, size).build();
        }


        public static String buildPetSelection (Uri uri) {
            StringBuilder builder = new StringBuilder("");
            String name = uri.getQueryParameter(COLUMN_NAME);
            if (name != null) {
                builder.append(PetContract.PetsEntry.COLUMN_NAME + " = ?");
            }

            List<String> species = uri.getQueryParameters(COLUMN_SPECIES);
            if (!species.isEmpty()) {
                if (!builder.toString().equals("")) {
                    builder.append(" AND ");
                }
                builder.append(COLUMN_SPECIES + " IN (" + makePlaceholders(species.size()) + ")");
            }

            String sex = uri.getQueryParameter(COLUMN_GENDER);
            if (sex != null) {
                if (!builder.toString().equals("")) {
                    builder.append(" AND ");
                }
                builder.append(COLUMN_GENDER + " = ?");
            }

            List<String> sizes = uri.getQueryParameters(COLUMN_SIZE);
            if (!sizes.isEmpty()) {
                if (!builder.toString().equals("")) {
                    builder.append(" AND ");
                }
                builder.append(COLUMN_SIZE + " IN (" + makePlaceholders(sizes.size()) + ")");
            }

            Log.d("PRUEBA", builder.toString());
            return builder.toString();
        }

        private static String makePlaceholders(int num) {
            String st = "?";
            for (int i=1; i<num; i++) {
                st += ", ?";
            }
            return st;
        }

        public static String[] buildPetParamsSelection(Uri uri) {
            ArrayList<String> params = new ArrayList<>();
            String name = uri.getQueryParameter(COLUMN_NAME);
            if (name != null) {
                params.add(name);
            }
            List<String> species = uri.getQueryParameters(COLUMN_SPECIES);
            if (!species.isEmpty()) {
                for (int i=0; i<species.size(); i++) {
                    params.add(species.get(i));
                }
            }
            String sex = uri.getQueryParameter(COLUMN_GENDER);
            if (sex != null) {
                params.add(sex);
            }

            List<String> sizes = uri.getQueryParameters(COLUMN_SIZE);
            if (!sizes.isEmpty()) {
                for (int i=0; i<sizes.size(); i++) {
                    params.add(sizes.get(i));
                }
            }

            return params.toArray(new String[params.size()]);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}
