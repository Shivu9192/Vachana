package com.akash.vachana.dbUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by akash on 9/13/16.
 */
public class MainDbHelper extends SQLiteOpenHelper implements Serializable {
    private static final String TAG = "MainDbHelper";
    private static String DB_PATH;

    public static final String DATABASE_NAME = "main.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_KATHRU = "Kathru";
    public static final String KEY_KATHRU_ID = "Id";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ANKITHA = "Ankitha";
    public static final String KEY_NUMBER  = "Num";
    public static final String KEY_DETAILS = "Details";

    ////// Vachana Table
    public static final String TABLE_VACHANA = "Vachana";
    public static final String KEY_VACHANA_ID = "Id";
    public static final String KEY_TEXT = "Txt";
    public static final String KEY_TITLE = "Title";
    public static final String FOREIGN_KEY_KATHRU_ID = "KathruId";
    public static final String KEY_FAVORITE = "Favorite";;

    private static Context mContext;
    private static SQLiteDatabase mDataBase;

    public MainDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 4.2) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH =  "/data/data/" + context.getPackageName() + "/databases/";
        }
        mContext = context;
    }


    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
    public void getDataBase() throws IOException {

        // If database not exists copy it from the assets
        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                // Copy the database from assests
                copyDataBase();
                Log.e("DataBaseHelper", "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("Error Copying DataBase");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        return dbFile.exists();
    }


    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DB_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();

    }

    public void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if (mDataBase != null)
            mDataBase.close();

        super.close();

    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
/*
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_KATHRU + "("
                + KEY_KATHRU_ID+ " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_ANKITHA + "TEXT,"
                + KEY_NUMBER + "INT,"
                + KEY_DETAILS + "TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_CONTACTS_TABLE);
*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_KATHRU);
        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public KathruMini getKathruMiniById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_KATHRU, new String[] { KEY_KATHRU_ID,
                        KEY_NAME, KEY_ANKITHA, KEY_NUMBER, KEY_FAVORITE },
                KEY_KATHRU_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        String name = cursor.getString(1);

        KathruMini kathruMini = new KathruMini(Integer.parseInt(cursor.getString(0)), name, cursor.getString(2),
                cursor.getInt(3), cursor.getInt(3));

        cursor.close();

        return kathruMini;

    }

    public ArrayList<KathruMini> getAllKathruMinis(){
        ArrayList<KathruMini> contactList = new ArrayList<>();
        String selectQuery = "SELECT " +
                KEY_KATHRU_ID + ", " +
                KEY_NAME + ", " +
                KEY_ANKITHA + ", " +
                KEY_NUMBER + ", " +
                KEY_FAVORITE +
                " FROM " + TABLE_KATHRU + " ORDER BY Name";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String name = cursor.getString(1);
                String ankitha = cursor.getString(2);
                int num = cursor.getInt(3);
                int favorite = cursor.getInt(4);

                KathruMini contact = new KathruMini(id, name, ankitha, num, favorite);

                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return contactList;
    }

    public ArrayList<VachanaMini> getVachanaMinisByKathruId (int kathruId, String kathruName) {
        ArrayList<VachanaMini> vachanaMinis = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VACHANA, new String[] { KEY_VACHANA_ID, KEY_TITLE, KEY_FAVORITE},
                FOREIGN_KEY_KATHRU_ID + "=?",
                new String[] { String.valueOf(kathruId) }, null, null, KEY_TITLE, null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String title = cursor.getString(1);
                VachanaMini vachanaMini = new VachanaMini(id, kathruId, kathruName, title, cursor.getInt(2));
                vachanaMinis.add(vachanaMini);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return vachanaMinis;
    }

    public ArrayList<VachanaMini> getVachanaMinisByKathruId (int kathruId) {
        return  getVachanaMinisByKathruId(kathruId, getKathruNameById(kathruId));
    }

    public String getKathruNameById(int kathruId) { return getKathruMiniById(kathruId).getName(); }

    public ArrayList<VachanaMini> query(String rawQuery, String[] parameters) {
        ArrayList<VachanaMini> vachanaMinis = new ArrayList<>();
        if (parameters[0].length()<3)
            return vachanaMinis;

        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(TABLE_VACHANA, fields, q, parameter, null, null, null, "100");
        Cursor cursor = db.rawQuery(rawQuery, parameters);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String title = cursor.getString(1);
                int kathruId = cursor.getInt(2);
                VachanaMini vachanaMini = new VachanaMini(id, kathruId, getKathruNameById(kathruId),
                        title, cursor.getInt(2));
                vachanaMinis.add(vachanaMini);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vachanaMinis;
    }

    public Vachana getVachana(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VACHANA, new String[] { KEY_VACHANA_ID, KEY_TEXT, FOREIGN_KEY_KATHRU_ID,
                KEY_FAVORITE},
                KEY_VACHANA_ID+ "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        String text = cursor.getString(1);
        int kathruId = Integer.parseInt(cursor.getString(2));
        Vachana vachana = new Vachana(id, text, getKathruNameById(kathruId), cursor.getInt(3) == 1, kathruId);
        cursor.close();
        return vachana;
    }

    public void addVachanaToFavorite(int vachanaId){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FAVORITE, 1);

        String[] args = new String[]{String.valueOf(vachanaId)};
        db.update(TABLE_VACHANA, newValues, KEY_VACHANA_ID+"=?", args);
    }

    public void removeVachanaFromFavorite(int vachanaId){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FAVORITE, 0);

        String[] args = new String[]{String.valueOf(vachanaId)};
        db.update(TABLE_VACHANA, newValues, KEY_VACHANA_ID+"=?", args);
    }

    public ArrayList<VachanaMini> getFavoriteVachanaMinis() {
        ArrayList<VachanaMini> vachanaMinis = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VACHANA, new String[] { KEY_VACHANA_ID, KEY_TITLE, FOREIGN_KEY_KATHRU_ID},
                KEY_FAVORITE + "=?",
                new String[] { String.valueOf(1) }, null, null, KEY_TITLE, null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String title = cursor.getString(1);
                int kathruId = cursor.getInt(2);
                VachanaMini vachanaMini = new VachanaMini(id, kathruId, getKathruNameById(kathruId),
                        title, 1);
                vachanaMinis.add(vachanaMini);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vachanaMinis;
    }

    public ArrayList<KathruMini> getFavoriteKathruMinis() {
        ArrayList<KathruMini> kathruMinis = new ArrayList<>();
        String selectQuery = "SELECT " +
                KEY_KATHRU_ID + ", " +
                KEY_NAME + ", " +
                KEY_ANKITHA + ", " +
                KEY_NUMBER + ", " +
                KEY_FAVORITE +
                " FROM " + TABLE_KATHRU + " WHERE " + KEY_FAVORITE +
                " = 1 ORDER BY Name";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String name = cursor.getString(1);
                String ankitha = cursor.getString(2);
                int num = cursor.getInt(3);
                int favorite = cursor.getInt(4);

                KathruMini contact = new KathruMini(id, name, ankitha, num, favorite);

                kathruMinis.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return kathruMinis;
    }

    public void addKathruToFavorite(int kathruId){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FAVORITE, 1);

        String[] args = new String[]{String.valueOf(kathruId)};
        db.update(TABLE_KATHRU, newValues, KEY_KATHRU_ID+"=?", args);
    }

    public void removeKathruFromFavorite(int vachanaId){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FAVORITE, 0);

        String[] args = new String[]{String.valueOf(vachanaId)};
        db.update(TABLE_KATHRU, newValues, KEY_KATHRU_ID+"=?", args);
    }
}
