package com.singbis.examsingbis;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by archie on 5/2/16.
 */
class DataImporter extends AsyncTask<String, Integer, Boolean> {

    public interface Events {
        public void onProgress(int progress);
        public void Finished(boolean done);
    }

    private Events importEvents = null;

    public void setEvents(Events events) {
        importEvents = events;
    }
    public void setContext(Context context) {
        currentContext = context;
    }

    private String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
    private Context currentContext;

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     * */

    @Override
    protected Boolean doInBackground(String... f_url) {

        if(currentContext == null)
            return false;

        DataBaseHelper db = new DataBaseHelper(currentContext);
        db.openDataBase();
        db.reset();

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        boolean first_line = true;
        int count = 0;
        boolean success = false;

        try {
            br = new BufferedReader(new FileReader(f_url[0]));
            while ((line = br.readLine()) != null) {

                if(first_line) {
                    first_line = false;
                    continue;
                }

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                if(data.length != 3)
                    return false;

                DataBaseHelper.Product product = new DataBaseHelper.Product();
                product.sku = data[0];
                product.productName = data[1];
                product.productPrice = Double.parseDouble(data[2]);

                db.addProduct(product);
                count++;

                if(count % 250 == 0)
                    publishProgress(count);
            }
            success = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e)  {
            e.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            db.close();
        }

        return success;
    }

    /**
     * Updating progress bar
     * */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        // setting progress percentage
        if(importEvents != null)
            importEvents.onProgress(progress[0]);
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(Boolean result) {
        // dismiss the dialog after the file was downloaded
        if(importEvents != null)
            importEvents.Finished(result);

    }

}
