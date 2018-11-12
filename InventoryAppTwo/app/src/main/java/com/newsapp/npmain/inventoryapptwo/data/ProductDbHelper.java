package com.newsapp.npmain.inventoryapptwo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.ProductEntry.*;

public class ProductDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "bookstore.db";
    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " +  TABLE_NAME +
                    " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_PRICE + " INT NOT NULL DEFAULT 0, " +
                    COLUMN_PRODUCT_QUANTITY + " INT NOT NULL DEFAULT 0, " +
                    COLUMN_PRODUCT_SUPPLIER_NAME + " STRING, "  +
                    COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER + " STRING " +
                    " );";
    private static final String SQL_DELETE_PRODUCTS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ProductDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_PRODUCTS_TABLE);
        onCreate(db);
    }
}
