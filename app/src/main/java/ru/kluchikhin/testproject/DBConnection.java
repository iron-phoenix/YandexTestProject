package ru.kluchikhin.testproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DBConnection {

    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "mytab";

    public static final String COLUMN_AUTO_ID = "_id";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PARENT_ID = "parent_id";

    private static final String DB_CREATE =
                    "create table " + DB_TABLE + "(" +
                    COLUMN_AUTO_ID + " integer primary key autoincrement, " +
                    COLUMN_ID + " integer, " +
                    COLUMN_TITLE + " text, " +
                    COLUMN_PARENT_ID + " integer" +
                    ");";

    private final Context ctx;


    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBConnection(Context ctx) {
        this.ctx = ctx;
    }

    public void dumpList(List<Category> categories) {
        for (Category c: categories) {
            addEntity(c, 0);
        }
    }

    public void addEntity(Category c, long parent_id) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, c.getId());
        cv.put(COLUMN_TITLE, c.getTitle());
        cv.put(COLUMN_PARENT_ID, parent_id);
        long id = db.insert(DB_TABLE, null, cv);
        if (c.getSubs() == null) return;
        for (Category sub: c.getSubs()) {
            addEntity(sub, id);
        }
    }

    public void getList(long parent_id, List<Category> result) {
        result.clear();
        Cursor cursor = db.query(DB_TABLE,
                null,
                COLUMN_PARENT_ID + " = ?",
                new String[]{String.valueOf(parent_id)},
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            int column_Id = cursor.getColumnIndex(COLUMN_AUTO_ID);
            int columnId = cursor.getColumnIndex(COLUMN_ID);
            int columnTitle = cursor.getColumnIndex(COLUMN_TITLE);
            do {
                result.add(new Category(cursor.getInt(column_Id), cursor.getInt(columnId), cursor.getString(columnTitle)));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void clear() {
        db.execSQL("delete from "+ DB_TABLE);
    }

    public void open() {
        dbHelper = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}
