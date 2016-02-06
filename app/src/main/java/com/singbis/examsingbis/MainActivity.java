package com.singbis.examsingbis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        productList = (ListView) findViewById(R.id.listViewProductData);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/csv");
                try {
                    startActivityForResult(Intent.createChooser(intent, "Please select *.csv file to import"), 777);
                }  catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Please install file manager",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductDialog("", "", "", false);
            }
        });

        populateProductList();

    }

    private void showProductDialog(String sku_value, String product_value, String price_value, final boolean update) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText sku = (EditText) promptsView
                .findViewById(R.id.editTextDialogSku);
        final EditText product_name = (EditText) promptsView
                .findViewById(R.id.editTextDialogProductName);
        final EditText product_price = (EditText) promptsView
                .findViewById(R.id.editTextDialogProductPrice);

        sku.setText(sku_value);
        if(update)
            sku.setEnabled(false);
        product_name.setText(product_value);
        product_price.setText(price_value);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton((update) ? "Update" : "Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                DataBaseHelper.Product product = new DataBaseHelper.Product();
                                product.sku = sku.getText().toString();
                                product.productName = product_name.getText().toString();
                                try {
                                    product.productPrice = Double.parseDouble(product_price.getText().toString());
                                } catch (Exception e) {
                                    return;
                                }

                                if(product.sku.trim().isEmpty() || product.productName.trim().isEmpty())
                                    return;

                                DataBaseHelper db = new DataBaseHelper(MainActivity.this);
                                db.openDataBase();
                                if(!update)
                                    db.addProduct(product);
                                else
                                    db.updateProduct(product);
                                db.close();
                                populateProductList();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void populateProductList() {

        DataBaseHelper db = new DataBaseHelper(this);
        db.openDataBase();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                db.getAllProducts(),     // Pass in the cursor to bind to.
                new String[] {"product_name", "sku"}, // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        productList.setAdapter(adapter);
        db.close();


        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView sku = (TextView)view.findViewById(android.R.id.text2);
                DataBaseHelper db = new DataBaseHelper(MainActivity.this);
                db.openDataBase();
                DataBaseHelper.Product product = db.getProduct(sku.getText().toString());
                db.close();
                showProductDialog(product.sku, product.productName, product.productPrice.toString(), true);
            }
        });

        productList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final TextView sku = (TextView)view.findViewById(android.R.id.text2);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                DataBaseHelper db = new DataBaseHelper(MainActivity.this);
                                db.openDataBase();
                                db.deleteProduct(sku.getText().toString());
                                db.close();
                                populateProductList();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure that you want to delete this record?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 777) {
            if(resultCode == Activity.RESULT_OK){

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Set<String> keys = bundle.keySet();
                    Iterator<String> it = keys.iterator();

                    while (it.hasNext()) {
                        String key = it.next();
                        String file_uri = data.getStringExtra(key);
                        File file = new File(file_uri);
                        if(file.exists()) {
                            parseAndPopulateProducts(file_uri);
                            break;
                        }
                    }

                } else {
                    Uri uri = data.getData();
                    String file_uri = uri.getPath();
                    File file = new File(file_uri);
                    if(file.exists())
                        parseAndPopulateProducts(file_uri);
                    else {
                        try {
                            String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/sb_data.csv";
                            InputStream input = getContentResolver().openInputStream(data.getData());
                            OutputStream out = new FileOutputStream(file_path);
                            byte[] buf = new byte[1024];
                            int len;
                            while((len=input.read(buf))>0){
                                out.write(buf,0,len);
                            }
                            out.close();
                            input.close();

                            parseAndPopulateProducts(file_path);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Cannot open file!",
                                    Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Cannot open file!",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void parseAndPopulateProducts(String csvFile) {

        final ProgressDialog downloadDialog = new ProgressDialog(this);
        downloadDialog.setMessage("Populating records...");
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setIndeterminate(true);
        downloadDialog.setProgressNumberFormat(null);
        downloadDialog.setProgressPercentFormat(null);
        downloadDialog.show();
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.setCancelable(false);

        DataImporter data_importer = new DataImporter();
        data_importer.setContext(this);
        data_importer.setEvents(new DataImporter.Events() {
            @Override
            public void onProgress(int progress) {
                downloadDialog.setMessage("Populating records..." + progress);
            }

            @Override
            public void Finished(boolean done) {
                downloadDialog.dismiss();
                if(!done)
                    Toast.makeText(MainActivity.this, "Wrong file format!",
                            Toast.LENGTH_LONG).show();
                populateProductList();
            }
        });
        data_importer.execute(csvFile);
    }

}
