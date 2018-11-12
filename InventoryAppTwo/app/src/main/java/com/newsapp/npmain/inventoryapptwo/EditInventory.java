package com.newsapp.npmain.inventoryapptwo;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

import static com.newsapp.npmain.inventoryapptwo.data.ProductContract.ProductEntry.*;

public class EditInventory extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private EditText productNameText;
    private EditText productPriceText;
    private EditText productQuantityText;
    private EditText supplierNameText;
    private EditText supplierPhoneText;
    private Button btnAddQuantity;
    private Button btnMinusQuantity;
    private Button btnSaveNewProduct;
    private Button btnUpdateProduct;
    private Button btnDeleteProduct;
    private Button btnCallSupplier;


    private static final int LOADER_EDIT_ID = 2;
    private static final String EMPTY_STRING = "";
    private static final int MIN_PRICE_AND_QUANTITY = 0;
    private static final int MAX_PRICE = 10000000;
    private static final int MAX_QUANTITY = 200;
    private Uri currentProductUri;
    private boolean productHasChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory);
        final Context context = this;
        productNameText = findViewById(R.id.edit_product_name);
        productNameText.setOnTouchListener(touchListener);
        productPriceText = findViewById(R.id.edit_product_price);
        productPriceText.setOnTouchListener(touchListener);
        productQuantityText = findViewById(R.id.edit_product_quantity);
        productQuantityText.setOnTouchListener(touchListener);
        supplierNameText = findViewById(R.id.edit_supplier_name);
        supplierNameText.setOnTouchListener(touchListener);
        supplierPhoneText = findViewById(R.id.edit_supplier_phone);
        supplierPhoneText.setOnTouchListener(touchListener);
        btnAddQuantity = findViewById(R.id.button_product_quantity_add);
        btnAddQuantity.setOnClickListener(view ->
        {
            final String productQuantityTextValue = productQuantityText.getText().toString();
            if (isNotValidString(productQuantityTextValue))
            {
                Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE),Toast.LENGTH_SHORT).show();
            }
            else
            {
                int quantity = Integer.parseInt(productQuantityTextValue);
                quantity++;
                if (!isNotInRange(quantity, MIN_PRICE_AND_QUANTITY, MAX_PRICE))
                {
                    productQuantityText.setText(Integer.toString(quantity));
                }
            }
        });
        btnMinusQuantity = findViewById(R.id.button_product_quantity_minus);
        btnMinusQuantity.setOnClickListener(view ->
        {
            final String productQuantityTextValue = productQuantityText.getText().toString();
            if (isNotValidString(productQuantityTextValue))
            {
                Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE),Toast.LENGTH_SHORT).show();
            }
            else
            {
                int quantity = Integer.parseInt(productQuantityTextValue);
                quantity--;
                if (!isNotInRange(quantity, MIN_PRICE_AND_QUANTITY, MAX_PRICE))
                {
                    productQuantityText.setText(Integer.toString(quantity));
                }
            }
        });
        btnSaveNewProduct = findViewById(R.id.button_product_save);
        btnCallSupplier = findViewById(R.id.button_supplier_call);
        btnUpdateProduct = findViewById(R.id.button_product_update);
        btnDeleteProduct = findViewById(R.id.button_product_delete);
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null)
        {
            setTitle(R.string.EDIT_INVENTORY_PAGE_TITLE_ADD_NEW);
            btnCallSupplier.setVisibility(View.GONE);
            btnUpdateProduct.setVisibility(View.GONE);
            btnDeleteProduct.setVisibility(View.GONE);
            btnSaveNewProduct.setVisibility(View.VISIBLE);
            Button btnSaveNewProduct = findViewById(R.id.button_product_save);
            btnSaveNewProduct.setOnClickListener(view ->
            {
                saveProduct();
                finish();
            });
        }
        else
        {
            setTitle(R.string.EDIT_INVENTORY_PAGE_TITLE_UPDATE);
            btnSaveNewProduct.setVisibility(View.GONE);
            btnCallSupplier.setVisibility(View.VISIBLE);

            btnCallSupplier.setOnClickListener(view ->
            {
                String tempSupplierPhoneTextValue = supplierPhoneText.getText().toString().trim();
                if (isNotValidString(tempSupplierPhoneTextValue))
                {
                    Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (isValidPhoneNumber(tempSupplierPhoneTextValue))
                    {
                        Intent phoneIntent =  new Intent(Intent.ACTION_DIAL);
                        phoneIntent.setData(Uri.parse("tel:" + tempSupplierPhoneTextValue));
                        if (phoneIntent.resolveActivity(getPackageManager())!= null)
                        {
                            startActivity(phoneIntent);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btnUpdateProduct.setVisibility(View.VISIBLE);
            btnUpdateProduct.setOnClickListener(view ->
            {
                saveProduct();
                finish();
            });
            btnDeleteProduct.setVisibility(View.VISIBLE);
            btnDeleteProduct.setOnClickListener(view -> showDeleteConfirmationDialog());
            getSupportLoaderManager().initLoader(LOADER_EDIT_ID, null, this);
        }
    }

    private void showDeleteConfirmationDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, (dialog, id) ->
        {
            // User clicked the "Delete" button, so delete the Product.
            deleteProduct();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) ->
        {
            // User clicked the "Cancel" button, so dismiss the dialog and continue editing the product.
            if (dialog != null)
            {
                dialog.dismiss();
            }

        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct()
    {

        if (currentProductUri != null)
        {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0)
            {
                Context context = getApplicationContext();
                Toast.makeText( context, context.getString(R.string.ERROR_PRODUCT_DELETE_RESULT), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private View.OnTouchListener touchListener = (view, motionEvent) ->
    {
        productHasChanged = true;
        return false;
    };

    @Override
    public void onBackPressed()
    {
        if (!productHasChanged)
        {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = ((dialogInterface, i) ->  finish());
        showUnsavedChangesDialog(discardButtonClickListener);

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, ((dialog, i) ->
       {
            if (dialog != null)
            {
                dialog.dismiss();
            }
        }));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle)
    {
        if (currentProductUri == null)
        {
            return null;
        }
        String[] projection = {_ID,
                COLUMN_PRODUCT_NAME,
                COLUMN_PRODUCT_PRICE,
                COLUMN_PRODUCT_QUANTITY,
                COLUMN_PRODUCT_SUPPLIER_NAME,
                COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};
        CursorLoader loader = new CursorLoader(
                this,
                currentProductUri,
                projection,
                null,
                null,
                null);
        return loader;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            if (!productHasChanged)
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            DialogInterface.OnClickListener discardButtonClickListener = (dialogInterface, i) ->
            NavUtils.navigateUpFromSameTask(EditInventory.this);
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
    {
        if (cursor == null || cursor.getCount() < 1)
        {
            return;
        }
        if (cursor.moveToFirst())
        {
            productNameText.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
            productPriceText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE))));
            productQuantityText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_QUANTITY))));
            supplierNameText.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_SUPPLIER_NAME)));
            supplierPhoneText.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)
    {
        productNameText.setText(EMPTY_STRING);
        productPriceText.setText(EMPTY_STRING);
        productQuantityText.setText(EMPTY_STRING);
        supplierNameText.setText(EMPTY_STRING);
        supplierPhoneText.setText(EMPTY_STRING);
    }

    private void saveProduct()
    {
        final Context context = this;
        String toastMessage = EMPTY_STRING;
        int price = 0;
        int quantity = 0;
        ContentValues values = new ContentValues();
        String productName = productNameText.getText().toString().trim();
        String tempProductPrice = productPriceText.getText().toString().trim();
        String productQuantity = productQuantityText.getText().toString().trim();
        String supplierName = supplierNameText.getText().toString().trim();
        String supplierPhone = supplierPhoneText.getText().toString().trim();
        if (isNotValidString(productName)) {
            toastMessage = context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_NAME);
        }

        if (isNotValidString(tempProductPrice)) {
            toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE);
        }
        else
        {
            // drop all characters except digits from the price
            String newPrice = tempProductPrice.replaceAll("[^\\d]+",EMPTY_STRING);
            price = Integer.parseInt(newPrice);
            if (isNotInRange(price, MIN_PRICE_AND_QUANTITY, MAX_PRICE))
            {
                toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_PRICE_RANGE, MIN_PRICE_AND_QUANTITY, MAX_QUANTITY);
            }
        }
        if (isNotValidString(productQuantity))
        {
            toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY);
        }
        else
        {
            quantity = Integer.parseInt(productQuantity);
            if (isNotInRange(quantity, MIN_PRICE_AND_QUANTITY, MAX_QUANTITY))
            {
                toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_QUANTITY_RANGE, MIN_PRICE_AND_QUANTITY, MAX_QUANTITY);
            }
        }
        if (isNotValidString(supplierName))
        {
            toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_NAME);
        }
        if (isNotValidString(supplierPhone) && isValidPhoneNumber(supplierPhone))
        {
            toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE);
        }
        else if (!isValidPhoneNumber(supplierPhone))
        {
            toastMessage += context.getString(R.string.ERROR_PRODUCT_UPDATE_PRODUCT_SUPPLIER_PHONE);
        }
        // ((currentProductUri == null) &&
        if (!toastMessage.equals(EMPTY_STRING))
        {
            Toast.makeText(context,toastMessage,Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(COLUMN_PRODUCT_NAME,productName);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
        values.put(COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhone);

        if (currentProductUri == null)
        {
            Uri newUri = getContentResolver().insert(CONTENT_URI, values);
            if (newUri == null)
            {
                Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_INSERT_PROVIDER), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            int rowsUpdated = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsUpdated == 0)
            {
                Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_PROVIDER), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context, context.getString(R.string.ERROR_PRODUCT_UPDATE_RESULT_SAVE, rowsUpdated), Toast.LENGTH_SHORT).show();
            }
        }

    }
    private boolean isNotValidString(String strValue)
    {
        if ((strValue == null ) || TextUtils.isEmpty(strValue))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    private boolean isNotInRange(int value, int minValue, int maxValue)
    {
        if ((value < minValue ) || value > maxValue)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber)
    {
        // strip out all characters except digits from phone number
        String supplierPhoneTextValue = phoneNumber.replaceAll("[^\\d]+",EMPTY_STRING);
        if (supplierPhoneTextValue.matches("^[+]?[0-9]{10,13}$"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
