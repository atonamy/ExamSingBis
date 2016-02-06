package com.singbis.examsingbis;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by archie on 5/2/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static class Employee {
        public int id;
        public String userName;
        public String password;
    }

    public static class Product {
        public String sku;
        public String productName;
        public Double productPrice;
    }

    //The Android's default system path of your application database.
    private static String DB_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";

    private static String DB_NAME = "dbexam.db";

    private SQLiteDatabase myDataBase = null;

    private final Context myContext;

    public static final String PRODUCTS = "CREATE TABLE IF NOT EXISTS Product (_id INTEGER PRIMARY KEY, sku TEXT NOT NULL, product_name TEXT NOT NULL, product_price REAL NOT NULL)";
    public static final String DELETE_PRODUCTS = "DROP TABLE Product";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }


    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            File file = new File(myPath);
            if(file.exists())
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            else
                return false;

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }


    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        if(checkDataBase()) {
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            initTables();
        }

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    private void initTables() {
        myDataBase.execSQL(PRODUCTS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.


    public void reset() {
        if(myDataBase == null)
            return;

        myDataBase.execSQL(DELETE_PRODUCTS);
        initTables();
    }

    public boolean addProduct(Product product) {

        if(myDataBase == null || product == null || product.sku == null || product.productName == null || product.productPrice == null)
            return false;

        String insertQuery = "INSERT INTO Product (_id, sku, product_name, product_price) " +
                "VALUES (NULL, '" + product.sku + "', '" + product.productName + "', " + product.productPrice + ")";
        try {
            myDataBase.execSQL(insertQuery);
        } catch(Exception e) {
            return false;
        }

        return true;
    }

    public boolean updateProduct(Product product) {

        if(myDataBase == null || product == null || product.sku == null || product.productName == null || product.productPrice == null)
            return false;

        String updateQuery = "UPDATE Product SET product_name = '" + product.productName + "', product_price = " + product.productPrice +
                " WHERE sku = '" + product.sku + "'";
        try {
            myDataBase.execSQL(updateQuery);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public boolean deleteProduct(String sku) {

        if(myDataBase == null || sku == null)
            return false;

        String deleteQuery = "DELETE FROM Product WHERE sku = '" + sku + "'";
        try {
            myDataBase.execSQL(deleteQuery);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public Cursor getAllProducts() {

        if(myDataBase == null)
            return null;

        String selectQuery = "SELECT * FROM Product" ;

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        return cursor;
    }

    public Product getProduct(String sku) {

        if(myDataBase == null)
            return null;

        Product product = null;
        // Select All Query
        String selectQuery = "SELECT * FROM Product WHERE sku ='" + sku + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            product = new Product();
            product.sku = cursor.getString(1);
            product.productName = cursor.getString(2);
            product.productPrice = cursor.getDouble(3);
        }

        return product;
    }


    public Employee getEmployee(String name) {

        if(myDataBase == null)
            return null;

        // Select All Query
        String selectQuery = "SELECT * FROM Employee WHERE e_username = '" + name + "'" ;

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        // getting row
        if (cursor.moveToFirst()) {

                Employee employee = new Employee();
                employee.id = cursor.getInt(0);
                employee.userName = cursor.getString(3);
                employee.password = cursor.getString(4);

                return employee;
        }

        return null;
    }


}