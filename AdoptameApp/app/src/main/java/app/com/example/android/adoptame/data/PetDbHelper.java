package app.com.example.android.adoptame.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import app.com.example.android.adoptame.data.PetContract.UsersEntry;
import app.com.example.android.adoptame.data.PetContract.PetsEntry;


/**
 * Crea la base de datos local
 */
public class PetDbHelper extends SQLiteOpenHelper {

    // Versión de la base de datos
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "adoptame.db";


    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Método que crea la base de datos
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetsEntry.TABLE_NAME + " (" +

                PetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PetsEntry.COLUMN_PICTURE + " TEXT UNIQUE NOT NULL, " +
                PetsEntry.COLUMN_NAME + " TEXT NULL, " +
                PetsEntry.COLUMN_GENDER + " TEXT NOT NULL, " +
                PetsEntry.COLUMN_SIZE + " TEXT NOT NULL, " +
                PetsEntry.COLUMN_AGE + " TEXT NULL, " +
                PetsEntry.COLUMN_WEIGHT + " TEXT NULL, " +
                PetsEntry.COLUMN_SPECIES + " TEXT NOT NULL, " +
                PetsEntry.COLUMN_BREED + " TEXT NULL, " +
                PetsEntry.COLUMN_DESCRIPTION + " TEXT NULL " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * Método que actualiza la base de datos a la nueva versión
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PetsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}