package app.com.example.android.adoptame.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Clase que controla las querys a la base de datos local
 */
public class PetsProvider extends ContentProvider {

    //Crea un uri matcher para saber a partir de una uri, que tipo de datos queremos pedir
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PetDbHelper mOpenHelper;

    static final int USERS = 100;
    static final int PETS = 200;
    static final int PET_WITH_ID = 201;
    static final int PET_WITH_SEARCH = 202;

    private static final String sPetIdSelection =
            PetContract.PetsEntry._ID + " = ? ";

    public static final String sPetNameSelection =
            PetContract.PetsEntry.COLUMN_NAME + " = ? ";

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PetContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PetContract.PATH_PETS + "/" + PetContract.PATH_SEARCH, PET_WITH_SEARCH);

        matcher.addURI(authority, PetContract.PATH_USERS, USERS);
        matcher.addURI(authority, PetContract.PATH_PETS, PETS);
        matcher.addURI(authority, PetContract.PATH_PETS + "/#", PET_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return PetContract.PetsEntry.CONTENT_TYPE;
            case PET_WITH_ID:
                return PetContract.PetsEntry.CONTENT_ITEM_TYPE;
            case PET_WITH_SEARCH:
                return PetContract.PetsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Método que a partir de una URI, gracias al URI matcher sabe el tipo de petición que se hace,
     * y realiza la query adecuada a la base de datos, con los parámetros necesarios
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case PETS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PetContract.PetsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case PET_WITH_ID: {
                String id = PetContract.PetsEntry.getIdFromUri(uri);
                retCursor =mOpenHelper.getReadableDatabase().query(
                        PetContract.PetsEntry.TABLE_NAME,
                        projection,
                        sPetIdSelection,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case PET_WITH_SEARCH: {
                String petSearchSelection = PetContract.PetsEntry.buildPetSelection(uri);
                String[] petParamsSelection = PetContract.PetsEntry.buildPetParamsSelection(uri);

                retCursor =mOpenHelper.getReadableDatabase().query(
                        PetContract.PetsEntry.TABLE_NAME,
                        projection,
                        petSearchSelection,
                        petParamsSelection,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Método que inserta en la tabla que indica la uri los valores de values
     * @param uri
     * @param values
     * @return uri con los datos insertados
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PETS: {
                long _id = db.insert(PetContract.PetsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PetContract.PetsEntry.buildPetIdUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    /**
     * Método que elimina datos de la tabla que indica la uri. Si se envía null, se envían todos los
     * datos de la tabla
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return el nº de columnas eliminadas
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case PETS:
                rowsDeleted = db.delete(
                        PetContract.PetsEntry.TABLE_NAME, selection, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);

        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Actualiza los datos necesarios de la base de datos
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case PETS:
                rowsUpdated = db.update(
                        PetContract.PetsEntry.TABLE_NAME, values, selection, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);

        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


     /* Método que inserta varios datos a la vez, siendo más eficiente que insert
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PetContract.PetsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        } else {
                            update(uri, value, PetContract.PetsEntry.COLUMN_PICTURE + "= ?", new String[]{value.get(PetContract.PetsEntry.COLUMN_PICTURE).toString()});
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}