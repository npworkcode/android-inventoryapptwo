package com.newsapp.npmain.inventoryapptwo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.ProductEntry.*;

public class ProductCursorAdapter extends CursorAdapter
{
    public ProductCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.tvProductName = view.findViewById(R.id.text_view_product_name);
        holder.tvProductPrice = view.findViewById(R.id.text_view_product_price);
        holder.tvProductQuantity = view.findViewById(R.id.text_view_product_quantity);
        holder.btnSale = view.findViewById(R.id.button_product_sale);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder holder = (ViewHolder) view.getTag();
        final int productId = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME));
        holder.tvProductName.setText(productName);
        long productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE));
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        String currency = nf.format(productPrice / 100.0);
        holder.tvProductPrice.setText(currency);
        int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_QUANTITY));

        holder.tvProductQuantity.setText(Integer.toString(productQuantity));
        // Don't display the Sale button if the quantity is 0
        if (productQuantity == 0)
        {
            holder.btnSale.setVisibility(View.GONE);
        }
        else
        {

            holder.btnSale.setOnClickListener((button) ->
            {
                Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, productId);
                ContentValues values = new ContentValues();
                final int updatedQuantity = productQuantity - 1;
                values.put(COLUMN_PRODUCT_NAME, productName);
                values.put(COLUMN_PRODUCT_PRICE, productPrice);
                values.put(COLUMN_PRODUCT_QUANTITY, updatedQuantity);
                int rowsUpdated = context.getContentResolver().update(currentProductUri, values, null, null);
            });
        }

    }

    static class ViewHolder
    {
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductQuantity;
        Button btnSale;
    }
}
