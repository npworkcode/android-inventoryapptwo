package com.newsapp.npmain.inventoryapptwo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.newsapp.npmain.inventoryapptwo.R;

import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.*;
import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.ProductEntry.*;

public class ProductProvider extends ContentProvider
{
    private ProductDbHelper productDbHelper;
    private static final int PRODUCTS = 1000;
    private static final int PRODUCTS_ID = 10001;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();
    
    static
    {
        uriMatcher.addURI(CONTENT_AUTHORITY,PATH_PRODUCTS,PRODUCTS);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#",PRODUCTS_ID);
    }
    
    @Override
    public boolean onCreate()
    {
        productDbHelper = new ProductDbHelper(getContext());
        return false;
    }

    
    @Nullable
    @Override
    public Cursor query( @NonNull Uri uri,  @Nullable String[] projection,  @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        SQLiteDatabase db = productDbHelper.getReadableDatabase();
        Cursor cursor;
        final Context context = getContext();
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case PRODUCTS:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_QUERY_PROVIDER, uri));
        }
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    
    @Nullable
    @Override
    public String getType( @NonNull Uri uri)
    {
        final int match = uriMatcher.match(uri);
        switch(match)
        {
            case PRODUCTS:
                return CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return CONTENT_LIST_TYPE;
            default:
                Context context = getContext();
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UNKNOWN_URI_TYPE, uri, match));
        }
    }

   
    @Nullable
    @Override
    public Uri insert( @NonNull Uri uri,  @Nullable ContentValues contentValues)
    {
        final Context context = getContext();
        final int match = uriMatcher.match(uri);
        switch(match)
        {
            case PRODUCTS:
                return insertProduct(uri,contentValues);
            default:
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_INSERT_PROVIDER, uri));
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values)
    {
        final Context context = getContext();
        String productName = values.getAsString(COLUMN_PRODUCT_NAME);
        if (productName == null  || TextUtils.isEmpty(productName))
        {
            throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_NAME));
        }

        String tempProductPrice = values.getAsString(COLUMN_PRODUCT_PRICE);
        if (tempProductPrice == null || TextUtils.isEmpty(tempProductPrice))
        {
            throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE));
        }
        else
        {
            int productPrice = Integer.parseInt(tempProductPrice);
            if (productPrice <= 0 )
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE));
            }
        }

        String tempProductQuantity = values.getAsString(COLUMN_PRODUCT_QUANTITY);
        if (tempProductQuantity == null || TextUtils.isEmpty(tempProductQuantity))
        {
            throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY));
        }
        else
        {
            int productQuantity = Integer.parseInt(tempProductQuantity);
            if (productQuantity < 0 )
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY));
            }
        }

        if (values.containsKey(COLUMN_PRODUCT_SUPPLIER_NAME))
        {
            String productSupplierName = values.getAsString(COLUMN_PRODUCT_SUPPLIER_NAME);
            if (productSupplierName == null  || TextUtils.isEmpty(productSupplierName))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_NAME));
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER))
        {
            String productSupplierPhone = values.getAsString(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            if (productSupplierPhone == null  || TextUtils.isEmpty(productSupplierPhone))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE));
            }
        }
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        long newRowId = db.insert(TABLE_NAME, null, values);
        if (newRowId == -1)
        {
            Log.e(LOG_TAG, context.getString(R.string.ERROR_PRODUCT_INSERT_PROVIDER));
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete( @NonNull Uri uri,  @Nullable String selection,  @Nullable String[] selectionArgs)
    {
        int rowsDeleted;
        final Context context = getContext();
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch(match)
        {
            case PRODUCTS:
                rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCTS_ID:
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_PROVIDER_DELETE, uri));
        }
        if (rowsDeleted != 0 )
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update( @NonNull Uri uri,  @Nullable ContentValues contentValues,  @Nullable String selection,  @Nullable String[] selectionArgs)
    {
        final Context context = getContext();
        final int match = uriMatcher.match(uri);
        switch( match )
        {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                selection = _ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PROVIDER, uri));
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final Context context = getContext();
        if (values.size() == 0 )
        {
            return 0;
        }
        if (values.containsKey(COLUMN_PRODUCT_NAME))
        {
            String productName = values.getAsString(COLUMN_PRODUCT_NAME);
            if (productName == null  || TextUtils.isEmpty(productName))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_NAME));
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_PRICE))
        {
            String tempProductPrice = values.getAsString(COLUMN_PRODUCT_PRICE);
            if (tempProductPrice == null || TextUtils.isEmpty(tempProductPrice))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE));
            }
            else
            {
                int productPrice = Integer.parseInt(tempProductPrice);
                if (productPrice <= 0 )
                {
                    throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE));
                }
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_QUANTITY))
        {
            String tempProductQuantity = values.getAsString(COLUMN_PRODUCT_QUANTITY);
            if (tempProductQuantity == null || TextUtils.isEmpty(tempProductQuantity))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY));
            }
            else
            {
                int productQuantity = Integer.parseInt(tempProductQuantity);
                if (productQuantity < 0 )
                {
                    throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY));
                }
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_SUPPLIER_NAME))
        {
            String productSupplierName = values.getAsString(COLUMN_PRODUCT_SUPPLIER_NAME);
            if (productSupplierName == null  || TextUtils.isEmpty(productSupplierName))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_NAME));
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER))
        {
            String productSupplierPhone = values.getAsString(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            if (productSupplierPhone == null  || TextUtils.isEmpty(productSupplierPhone))
            {
                throw new IllegalArgumentException(context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE));
            }
        }
        SQLiteDatabase db = productDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0)
        {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
