package com.newsapp.npmain.inventoryapptwo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.newsapp.npmain.inventoryapptwo.data.ProductDbHelper;

import java.util.Locale;

import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.ProductEntry.*;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private ProductDbHelper productDbHelper;
    private ProductCursorAdapter productCursorAdapter;
    private static final int PRODUCT_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Set up the Floating Button on Main screen to add a new Product
        FloatingActionButton invFloatingButton = findViewById(R.id.invbutton);
        invFloatingButton.setOnClickListener(view ->
        {
            Intent intent = new Intent(InventoryActivity.this, EditInventory.class);
            startActivity(intent);
        });
        ListView productListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.textview_empty_view);
        productListView.setEmptyView(emptyView);
        productCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(productCursorAdapter);
        productListView.setOnItemClickListener((adapterView, view, position, id) ->
        {
            Intent intent = new Intent(InventoryActivity.this, EditInventory.class);
            Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, id);
            intent.setData(currentProductUri);
            startActivity(intent);
        });
        getSupportLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);

    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle)
    {
        String[] projection = {_ID, COLUMN_PRODUCT_NAME, COLUMN_PRODUCT_PRICE, COLUMN_PRODUCT_QUANTITY};
        return new CursorLoader(
                this,
                CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
    {
        productCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)
    {
        productCursorAdapter.swapCursor(null);
    }
}
